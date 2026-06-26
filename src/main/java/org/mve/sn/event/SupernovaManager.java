package org.mve.sn.event;

import kotlin.Lazy;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function4;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KClass;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.channels.BufferOverflow;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.FlowKt;
import kotlinx.coroutines.flow.MutableSharedFlow;
import kotlinx.coroutines.flow.SharedFlowKt;
import net.mamoe.mirai.event.ConcurrencyKind;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.EventPriority;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.utils.MiraiLogger;
import org.jetbrains.annotations.NotNull;
import org.mve.asm.AccessFlag;
import org.mve.asm.ClassWriter;
import org.mve.asm.FieldWriter;
import org.mve.asm.MethodWriter;
import org.mve.asm.Opcodes;
import org.mve.asm.attribute.CodeWriter;
import org.mve.invoke.MagicAccessor;
import org.mve.invoke.common.JavaVM;
import org.mve.sn.core.Supernova;
import org.mve.uni.Concurrency;
import org.mve.uni.LazyJVM;
import org.mve.uni.Mirroring;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SupernovaManager<T extends Event> extends EventChannel<T> implements Runnable
{
	public static final MiraiLogger LOGGER = MiraiLogger.Factory.INSTANCE.create(SupernovaManager.class, "SupernovaManager");
	public static final SupernovaManager<Event> GLOBAL = new SupernovaManager<>(Event.class);
	public final MutableSharedFlow<T> flow = SharedFlowKt.MutableSharedFlow(0, 1, BufferOverflow.DROP_OLDEST);
	private final Map<Class<?>, ConcurrentLinkedQueue<Listener<? super Event>>> listeners = new ConcurrentHashMap<>();
	private final Class<T> type;
	private final Queue<Event> queue = new ConcurrentLinkedQueue<>();
	private boolean running = true;

	private SupernovaManager(Class<T> type)
	{
		super(Mirroring.checkcast(Reflection.getOrCreateKotlinClass(type)), EmptyCoroutineContext.INSTANCE);
		this.type = type;
	}

	@NotNull
	@Override
	public Flow<T> asFlow()
	{
		return FlowKt.filter(FlowKt.asSharedFlow(this.flow), (t, c) -> this.type.isInstance(t));
	}

	@NotNull
	@Override
	public EventChannel<T> context(@NotNull CoroutineContext... coroutineContexts)
	{
		return this;
	}

	@Override
	public <E extends Event> void registerListener(@NotNull KClass<? extends E> kClass, @NotNull Listener<? super E> listener)
	{
		this.listeners.computeIfAbsent(JvmClassMappingKt.getJavaClass(kClass), SupernovaManager::queue).add(Mirroring.checkcast(listener));
	}

	@NotNull
	@Override
	public <E extends Event> Listener<E> createListener(@NotNull CoroutineContext context, @NotNull ConcurrencyKind concurrencyKind, @NotNull EventPriority eventPriority, @NotNull Function2<? super E, ? super Continuation<? super ListeningStatus>, ?> function2)
	{
		return new SubscribeEvent<>(context.get(Mirroring.checkcast(Job.Key)), context, Mirroring.checkcast(function2), concurrencyKind);
	}

	@Override
	public void run()
	{
		while (this.running)
		{
			Event e = this.queue.poll();
			if (e == null)
			{
				synchronized (this)
				{
					Concurrency.wait(this, -1, false);
				}
				continue;
			}
			Set<Class<?>> set = new HashSet<>();
			Stack<Class<?>> stack = new Stack<>();
			stack.push(e.getClass());
			while (!stack.empty())
			{
				Class<?> clazz = stack.pop();
				if (clazz == null)
					continue;
				if (!set.contains(clazz))
				{
					set.add(clazz);
					ConcurrentLinkedQueue<Listener<? super Event>> orDefault = this.listeners.getOrDefault(clazz, null);
					if (orDefault != null)
					{
						//for (Listener<? super Event> listener : orDefault)
						for (Iterator<Listener<? super Event>> it = orDefault.iterator(); it.hasNext(); )
						{
							Listener<? super Event> listener = it.next();
							if (listener.isCancelled())
							{
								it.remove();
								continue;
							}
							ListeningStatus status = (ListeningStatus) listener.onEvent(e, Supernova.CONTINUATION);
							if (status == ListeningStatus.STOPPED)
								it.remove();
						}
					}
				}
				if (clazz.getSuperclass() != null)
					stack.push(clazz.getSuperclass());
				Class<?>[] interfaces = clazz.getInterfaces();
				for (Class<?> i : interfaces)
					stack.push(i);
			}
		}
	}

	public synchronized void broadcast(Event e)
	{
		this.queue.add(e);
		this.notify();
	}

	public synchronized void shutdown()
	{
		this.running = false;
		this.notify();
	}

	public static <E extends Event> ConcurrentLinkedQueue<Listener<? super E>> queue(Class<?> c)
	{
		return new ConcurrentLinkedQueue<>();
	}

	static
	{
		try
		{
			Class.forName("org.mve.sn.core.Supernova");
		}
		catch (ClassNotFoundException e)
		{
			throw new ExceptionInInitializerError(e);
		}

		LazyJVM<EventChannel<Event>> lazy = new LazyJVM<>(() -> SupernovaManager.GLOBAL);
		Mirroring.set(GlobalEventChannel.class, "instance$delegate", Lazy.class, lazy);
		Thread t = new Thread(SupernovaManager.GLOBAL, "SupernovaManager");
		t.setDaemon(false);
		t.start();
	}
}

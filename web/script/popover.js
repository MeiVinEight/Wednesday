function popoverClick(event, id)
{
	const popover = event.target;
	if (popover.id !== id)
		return;
	popover.style.opacity = "0";
	popover.style.pointerEvents = "none";
}
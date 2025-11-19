#define WINAPI __stdcall

typedef void *HANDLE;
typedef unsigned int DWORD;
typedef int BOOL;

HANDLE WINAPI GetStdHandle(DWORD nStdHandle);
BOOL WINAPI SetConsoleMode(HANDLE hConoleHandle, DWORD dwMode);
BOOL WINAPI GetConsoleMode(HANDLE hConsoleHandle, DWORD *lpMode);

#include "include/jni.h"
#include "include/org_mve_woden_Woden.h"

JNIEXPORT void JNICALL Java_org_mve_woden_Woden_reset0(JNIEnv *env, jclass class)
{
	HANDLE stdoutHandle = GetStdHandle((DWORD) -11);
	DWORD mode = 0;
	GetConsoleMode(stdoutHandle, &mode);
	SetConsoleMode(stdoutHandle, mode & (~8));
}
long _DllMainCRTStartup(void *handle, unsigned int reason, void *xxx)
{
	return 1;
}
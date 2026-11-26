package com.android.ssaid.abdullah;

interface IShellService {
    void destroy() = 16777114;
    String runCommand(String command) = 1;
    boolean grantWriteSecureSettings(String packageName) = 2;
    String getGlobalAndroidId() = 3;
    boolean setGlobalAndroidId(String newId) = 4;
}

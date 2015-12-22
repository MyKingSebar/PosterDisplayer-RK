package com.youngsee.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PackageInstaller {
	public boolean installSystemPkg(String fileName) {

		try {
			// execRootCommand("mount -o remount,rw /system");
			// execRootCommand("chmod 777 /system/app");
			RuntimeExec.getInstance().runRootCmd("mount -o remount,rw /system");
			RuntimeExec.getInstance().runRootCmd("chmod 777 /system/app");
			File src = new File(fileName);
			String destPath = "/system/app/YSSysController.apk";
			FileUtils.delFile("/system/app/YSSysController-ENC.apk");  // This patch just for clear all the controller service
			FileUtils.delFile(destPath);
			File dest = new File(destPath);

			if (!dest.exists()) {
				dest.createNewFile();
			}
			FileUtils.moveFileTo(src, dest);

			// execRootCommand("chown root.root " + destPath);
			// execRootCommand("chmod 644 " + destPath);
			// execRootCommand("chmod 755 /system/app");
			// execRootCommand("mount -o remount,ro /system");
			RuntimeExec.getInstance().runRootCmd("chown root.root " + destPath);
			RuntimeExec.getInstance().runRootCmd("chmod 644 " + destPath);
			RuntimeExec.getInstance().runRootCmd("chmod 755 /system/app");
			RuntimeExec.getInstance().runRootCmd("mount -o remount,ro /system");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	public boolean installBootAnimation(String fileName, String packageName) {
        try {
            RuntimeExec.getInstance().runRootCmd("mount -o remount,rw /system");
            RuntimeExec.getInstance().runRootCmd("chmod 777 /system/media");
            File src = new File(fileName);
            String destPath = "/system/media/" + packageName;
            FileUtils.delFile(destPath);
            File dest = new File(destPath);

            if (!dest.exists()) {
                dest.createNewFile();
            }
            FileUtils.moveFileTo(src, dest);

            RuntimeExec.getInstance().runRootCmd("chmod 755 /system/media");
            RuntimeExec.getInstance().runRootCmd("mount -o remount,ro /system");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
	
	public void execRootCommand(String command) throws IOException {
		Runtime runtime = Runtime.getRuntime();
		Process proc = runtime.exec("su\n");
		String temp = readInputStream(proc);
		temp = readErrorStream(proc);
		wait(proc);

		proc = runtime.exec(command + "\n");
		temp = readInputStream(proc);
		temp = readErrorStream(proc);

		// tv.setText(sb.toString());
		// 使用exec执行不会等执行成功以后才返回,它会立即返回
		// 所以在某些情况下是很要命的(比如复制文件的时候)
		// 使用wairFor()可以等待命令执行完成以后才返回
		wait(proc);
	}

	private String readInputStream(Process proc) {
		String line = "";
		StringBuilder sb = new StringBuilder(line);

		try {
			InputStream inputstream = proc.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(
					inputstream);
			BufferedReader bufferedreader = new BufferedReader(
					inputstreamreader);

			while ((line = bufferedreader.readLine()) != null) {
				// System.out.println(line);
				sb.append(line);
				sb.append('\n');
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}

		return sb.toString();
	}

	private String readErrorStream(Process proc) {
		String line = "";
		StringBuilder sb = new StringBuilder(line);

		try {
			InputStream inputstream = proc.getErrorStream();
			InputStreamReader inputstreamreader = new InputStreamReader(
					inputstream);
			BufferedReader bufferedreader = new BufferedReader(
					inputstreamreader);

			while ((line = bufferedreader.readLine()) != null) {
				// System.out.println(line);
				sb.append(line);
				sb.append('\n');
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}

		return sb.toString();
	}

	private void wait(Process proc) {
		try {
			if (proc.waitFor() != 0) {
				System.err.println("exit value = " + proc.exitValue());
			}
		} catch (InterruptedException e) {
			System.err.println(e);
		}
	}
}

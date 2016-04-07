package com.youngsee.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.youngsee.posterdisplayer.PosterApplication;

public class PackageInstaller {
	public boolean installSystemPkg(String fileName) {
	    boolean ret = false;
		try {
			RuntimeExec.getInstance().runRootCmd("mount -o remount,rw /system");
			RuntimeExec.getInstance().runRootCmd("chmod 777 /system/app");
			RuntimeExec.getInstance().runRootCmd("rm -f /system/app/YSSysCon*.apk");
			File src = new File(fileName);
			String destPath = "/system/app/YSSysController.apk";
			File dest = new File(destPath);

			if (!dest.exists()) {
				dest.createNewFile();
			}
			FileUtils.moveFileTo(src, dest);

			RuntimeExec.getInstance().runRootCmd("chown root.root " + destPath);
			RuntimeExec.getInstance().runRootCmd("chmod 644 " + destPath);
			RuntimeExec.getInstance().runRootCmd("chmod 755 /system/app");
			RuntimeExec.getInstance().runRootCmd("mount -o remount,ro /system");
			ret = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	public boolean installLib(String fileName, String packageName) {
	    boolean ret = false;
        try {
            RuntimeExec.getInstance().runRootCmd("mount -o remount,rw /system");
            RuntimeExec.getInstance().runRootCmd("chmod 777 /system/lib");
            File src = new File(fileName);
            String destPath = "/system/lib/" + packageName;
            FileUtils.delFile(destPath);
            File dest = new File(destPath);

            if (!dest.exists()) {
                dest.createNewFile();
            }
            FileUtils.moveFileTo(src, dest);

            RuntimeExec.getInstance().runRootCmd("chmod 777 " + destPath);
            RuntimeExec.getInstance().runRootCmd("chmod 755 /system/lib");
            RuntimeExec.getInstance().runRootCmd("mount -o remount,ro /system");
            ret = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }
	
	public String retrieveSourceFromAssets(String packageName){
        File filePath = PosterApplication.getInstance().getFilesDir();
        StringBuilder cachePath = new StringBuilder();
        cachePath.append(filePath.getAbsolutePath());
        cachePath.append("/");
        cachePath.append(packageName);
        
        try{
            File file = new File(cachePath.toString());
            if(!file.exists()){
                file.createNewFile();
            }
            InputStream is = PosterApplication.getInstance().getAssets().open(packageName);
            FileOutputStream fos = new FileOutputStream(file);
    
            byte[] temp = new byte[1024];
            int i = 0;
            while((i = is.read(temp)) != -1){
                fos.write(temp, 0, i);
            }
            fos.flush();
            fos.close();
            is.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        
        return cachePath.toString();
    }
}

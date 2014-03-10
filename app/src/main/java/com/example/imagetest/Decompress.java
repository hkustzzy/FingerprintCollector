package com.example.imagetest;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by rui on 14-2-21.
 */
public class Decompress {
    private String zip;
    private String loc;
    private InputStream fin;
    public Decompress(String zipFile, String location) {
        zip = zipFile;
        loc = location;
        fin = null;
        dirChecker("");
    }

    public Decompress(InputStream zipFile, String location) {
        fin = zipFile;
        loc = location;

        dirChecker("");
    }

    public void unzip() {
        try  {
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                Log.i("Decompress Unzipping ",ze.getName());

                if(ze.isDirectory()) {
                    dirChecker(ze.getName());
                } else {
                    int size;
                    byte[] buffer = new byte[2048];
                    FileOutputStream outStream = new FileOutputStream(loc + ze.getName());
                    BufferedOutputStream bufferOut = new BufferedOutputStream(outStream, buffer.length);
                    while((size = zin.read(buffer, 0, buffer.length)) != -1) {
                        bufferOut.write(buffer, 0, size);
                    }
                    zin.closeEntry();
                    bufferOut.flush();
                    bufferOut.close();
                }

            }
            zin.close();
        } catch(Exception e) {
            Log.i("Decompress unzip", ""+e.getCause());
        }

    }

    private void dirChecker(String dir) {
        File f = new File(loc + dir);

        if(!f.isDirectory()) {
            f.mkdirs();
        }
    }
}

package org.hackathon_ocw.androidclient;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by dianyang on 2016/2/29.
 */
public class Utils {

    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
                is.close();
                os.close();
            }
        }
        catch(Exception ex){}
    }
}

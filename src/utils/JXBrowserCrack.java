/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package utils;

/**
 * @author jvbor
 */

import com.teamdev.jxbrowser.chromium.bb;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;

/**
 * JXBrowser辅助工具类
 *
 * @author <a href="mailto:nathanael4ever@gmail.com">Nathanael Yang</a> 10/18/2017 10:09 AM
 */
public class JXBrowserCrack {

    static {
        try {
            Field e = bb.class.getDeclaredField("e");
            e.setAccessible(true);
            Field f = bb.class.getDeclaredField("f");
            f.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(e, e.getModifiers() & ~Modifier.FINAL);
            modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
            e.set(null, new BigInteger("1"));
            f.set(null, new BigInteger("1"));
            modifiersField.setAccessible(false);
            System.out.println("JXBrowser Cracked");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}

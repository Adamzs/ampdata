package lib.io.test;

import java.io.File;

import org.junit.Test;

import amp.lib.io.meta.Metadata;
import amp.lib.io.meta.MetafileManager;

public class TestMeta {

    @Test
    public void test() {
        MetafileManager.getMetafileHandler().readMetaFiles(new File("data"));
        for (Metadata md : Metadata.getAllMetadata()) {
            System.out.println(md.dump());
        }
    }

}

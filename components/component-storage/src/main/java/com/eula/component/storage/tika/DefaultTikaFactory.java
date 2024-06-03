package com.eula.component.storage.tika;

import org.apache.tika.Tika;

/**
 * @author xiankun.geng
 */
public class DefaultTikaFactory implements TikaFactory {

    private Tika tika;

    @Override
    public Tika getTika() {
        if (this.tika == null) {
            this.tika = new Tika();
        }
        return this.tika;
    }

}

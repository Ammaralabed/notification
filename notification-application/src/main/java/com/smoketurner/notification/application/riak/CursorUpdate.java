package com.smoketurner.notification.application.riak;

import static com.google.common.base.Preconditions.checkNotNull;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.basho.riak.client.api.commands.kv.UpdateValue;

public class CursorUpdate extends UpdateValue.Update<CursorObject> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CursorUpdate.class);
    private final String key;
    private final long value;

    /**
     * Constructor
     * 
     * @param key
     * @param value
     */
    public CursorUpdate(@Nonnull final String key, final long value) {
        this.key = checkNotNull(key);
        this.value = value;
    }

    @Override
    public CursorObject apply(CursorObject original) {
        if (original == null) {
            LOGGER.debug("original is null, creating new object");
            original = new CursorObject(key, value);
        }
        original.setValue(value);
        return original;
    }
}

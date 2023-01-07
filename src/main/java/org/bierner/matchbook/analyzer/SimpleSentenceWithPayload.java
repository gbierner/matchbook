package org.bierner.matchbook.analyzer;

import lombok.Getter;
import lombok.NonNull;

import java.util.Locale;

public class SimpleSentenceWithPayload<T> extends SimpleSentence {
    @Getter
    private final T payload;

    public SimpleSentenceWithPayload(@NonNull String text, @NonNull Locale locale, @NonNull Analyzer analyzer, T payload) {
        super(text, locale, analyzer);
        this.payload = payload;
    }

    protected SimpleAnnotatable getAnnotatable() {
        return new SimpleAnnotatableWithPayload(this);
    }

    protected class SimpleAnnotatableWithPayload extends SimpleAnnotatable {
        public SimpleAnnotatableWithPayload(@NonNull SimpleSentence delegate) {
            super(delegate);
        }

        public T getPayload() { return payload; }
    }
}

package org.moddingx.modlistcreator.util;

import joptsimple.util.EnumConverter;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class EnumConverters {
    
    public static <E extends Enum<E>> EnumConverter<E> enumArg(Class<E> cls) {
        return new ConcreteEnumConverter<>(cls);
    }
    
    private static class ConcreteEnumConverter<E extends Enum<E>> extends EnumConverter<E> {
        
        private ConcreteEnumConverter(Class<E> clazz) {
            super(clazz);
        }

        @Override
        public String valuePattern() {
            return Arrays.stream(this.valueType().getEnumConstants())
                    .map(v -> v.name().toLowerCase(Locale.ROOT))
                    .collect(Collectors.joining("|"));
        }
    }
}

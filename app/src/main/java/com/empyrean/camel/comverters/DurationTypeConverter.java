package com.empyrean.camel.comverters;

import org.apache.camel.Converter;

import java.time.Duration;

@Converter
public class DurationTypeConverter {

    @Converter
    public static Duration toDuration(String value) {
        return Duration.parse(value);
    }
}

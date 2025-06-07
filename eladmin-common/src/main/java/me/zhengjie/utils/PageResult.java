package me.zhengjie.utils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class PageResult<T> {

    private final List<T> content;

    private final long totalElements;

    public <V> PageResult<V> map(Function<? super T, ? extends V> converter) {
        List<V> newContent = this.content.stream()
                .map(converter)
                .collect(Collectors.toList());
        return new PageResult<>(newContent, totalElements);
    }
}

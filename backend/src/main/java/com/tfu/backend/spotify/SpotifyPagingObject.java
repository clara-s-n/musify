package com.tfu.backend.spotify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpotifyPagingObject<T> {
    private List<T> items = new ArrayList<>();
    private int total;
    private int limit;
    private int offset;
    
    public Stream<T> stream() {
        return items.stream();
    }
}
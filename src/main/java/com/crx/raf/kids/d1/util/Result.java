package com.crx.raf.kids.d1.util;

import java.util.function.Function;

public class Result<T> {
    public static <V> Result<V> of(V value){
        return new Result<>(value, null);
    }

    public static <V> Result<V> error(Error error){
        return new Result<>(null, error);
    }

    private T value;
    private Error error;

    private Result(T value, Error error) {
        this.value = value;
        this.error = error;
    }

    public T getValue() {
        return value;
    }

    public Error getError() {
        return error;
    }

    public boolean isError(){
        return error != null;
    }

    public <V> Result<V> flatMap(Function<T, Result<V>> mapper){
        if (isError()){
            return Result.error(error);
        }
        return mapper.apply(value);
    }

    @Override
    public String toString() {
        return "Result{" +
                "value=" + value +
                ", error=" + error +
                '}';
    }
}

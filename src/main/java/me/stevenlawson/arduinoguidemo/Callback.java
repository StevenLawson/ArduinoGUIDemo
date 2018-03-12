package me.stevenlawson.arduinoguidemo;

@FunctionalInterface
public interface Callback<T>
{
    public abstract void run(final T result);
}

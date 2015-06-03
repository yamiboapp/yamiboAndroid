package com.yamibo.main.yamibolib.locationservice.model;

public class SingleClassLoader extends ClassLoader {
    private final Class cl;
    private final String className;

    public SingleClassLoader(Class c) {
        cl = c;
        className = c.getName();
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        if (this.className.equals(className))
            return cl;
        else
            return super.loadClass(className);
    }
}

package com.github.unknownstudio.unknowndomain.engineapi.mod;

import java.net.URL;
import java.net.URLClassLoader;

public class ModClassLoader extends URLClassLoader {

	public ModClassLoader(URL[] urls) {
		super(urls);
	}
}

package de.unibonn.realkd.common;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 The Contributors of the realKD Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

/**
 */
public class TestFaultySerializationType {
	
	public static class CType  {
		
		@JsonProperty("list")
		private List<InterfaceSF<InterfaceP>> list;
		
		public CType(@JsonProperty("list") List<InterfaceSF<InterfaceP>> list) {
			this.list = list;
		}
		
	}
	
	public static class CType2  {
		
		@JsonProperty("list")
		private List<InterfaceSF<?>> list;
		
		public CType2(@JsonProperty("list") List<InterfaceSF<?>> list) {
			this.list = list;
		}
		
	}
	
	public static interface InterfaceSF<T> {
	}
	
	public static class ClassSF_P implements InterfaceSF<InterfaceP> {
		
		@JsonProperty("object")
		private InterfaceP object;
		
		public ClassSF_P(@JsonProperty("object") InterfaceP object) {
			this.object = object;
		}
		
	}
	
	public static interface InterfaceP {
	}
	
	public static class ClassP implements InterfaceP {
		
		@JsonProperty("string")
		String string;

		@JsonCreator
		public ClassP(@JsonProperty("string") String string) {
			this.string = string;
		}
		
	}
	
	public static interface InterfaceC<T> {
	}
	
	public static class ClassC<T> implements InterfaceC<T> {
		
		@JsonProperty("value")
		private T value;
		
		public ClassC(@JsonProperty("value") T value) {
			this.value = value;
		}
		
	}

	public static void main(String[] args) {
		testList();
		testCType();
		testCType2();
	}
	
	public static <T> String toJson(T object, Class<? super T> type) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enableDefaultTyping();
		try {
			return mapper.writeValueAsString(object);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T fromJson(String json, Class<T> type) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enableDefaultTyping();
		try {
			return mapper.readValue(json, type);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> void test(T object, Class<? super T> theClass) {
		try {
			String json = JsonSerialization.toJson(object);
			
			System.out.println("JSON:\t\t" + json);
			
			Object result = JsonSerialization.fromJson(json, theClass);
			
			System.out.println("OBJE:\t\t" + result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static ArrayList<InterfaceSF<InterfaceP>> getList() {
		InterfaceSF<InterfaceP> sf = new ClassSF_P(new ClassP("TeST"));
		
		ArrayList<InterfaceSF<InterfaceP>> list = Lists.newArrayList();
		list.add(sf);
		return  list;
	}

	private static ArrayList<InterfaceSF<?>> getList2() {
		InterfaceSF<InterfaceP> sf = new ClassSF_P(new ClassP("TeST"));
		return Lists.newArrayList(sf);
	}

	public static void testList() {
		System.out.println("testList");
		System.out.println("======================");
		
		test(getList(), ArrayList.class);
		
		System.out.println("======================");
		System.out.println();
	}

	public static void testCType() {
		System.out.println("testCType");
		System.out.println("======================");
		
		test(new CType(getList()), CType.class);
		
		System.out.println("======================");
		System.out.println();
	}
	
	public static void testCType2() {
		System.out.println("testCType2");
		System.out.println("======================");
		
		test(new CType2(getList2()), CType2.class);
		
		System.out.println("======================");
		System.out.println();
	}
	
}

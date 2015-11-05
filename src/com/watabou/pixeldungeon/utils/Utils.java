/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.utils;

import java.util.Locale;

import com.watabou.noosa.Game;
import com.odotopen.pixeldungeon.ml.R;

public class Utils {

	protected static final Class<?> strings      = getR_Field("string");
	protected static final Class<?> stringArrays = getR_Field("array");
	
	static private Class <?> getR_Field(String field){
		try {
			return Class.forName("com.odotopen.pixeldungeon.ml.R$"+field);
			//return Class.forName("com.nyrds.pixeldungeon.test.R$"+field);
		} catch (ClassNotFoundException e) {// well this is newer happens :) 
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String capitalize( String str ) {
		return Character.toUpperCase( str.charAt( 0 ) ) + str.substring( 1 );
	}
	
	public static String format( String format, Object...args ) {
		return String.format( Locale.ENGLISH, format, args );
	}
	
	public static String VOWELS	= "aoeiu";
	
	public static String indefinite( String noun ) {
		//In a pt_BR language(and another), there is no specific rule.
		if (Game.getVar(R.string.Utils_IsIndefinte).equals("0") ) {
		   return noun;
		}

		if (noun.length() == 0) {
			return "a";
		} else {
			return (VOWELS.indexOf( Character.toLowerCase( noun.charAt( 0 ) ) ) != -1 ? "an " : "a ") + noun;
		}
	}

	public static String[] getClassParams(String className ,String paramName, String[] defaultValues, boolean warnIfAbsent){
		
		if(className.length() == 0){ // isEmpty() require api level 9
			return defaultValues;
		}
		
		try{
			return Game.getVars(stringArrays.getField(className+"_"+paramName).getInt(null));
		}catch (NoSuchFieldException e){
			if(warnIfAbsent){
				GLog.w("no defination for  %s_%s :(", className, paramName);
			}
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return defaultValues;
	}

	
	public static String getClassParam(String className ,String paramName, String defaultValue, boolean warnIfAbsent){
		
		if(className.length() == 0){ // isEmpty() require api level 9
			return defaultValue;
		}
		
		try{
			String paramValue = Game.getVar(strings.getField(className+"_"+paramName).getInt(null));
			return paramValue;
		}catch (NoSuchFieldException e){
			if(warnIfAbsent){
				GLog.w("no defination for  %s_%s :(", className, paramName);
			}
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return defaultValue;
	}

	public static int genderFromString(String sGender) {
		int gender = Utils.NEUTER;
		
		if(sGender.equals("masculine")){
			gender = Utils.MASCULINE;
		}
		if(sGender.equals("feminine")){
			gender = Utils.FEMININE;
		}
		return gender;
	}
	
	public static final int NEUTER    = 0;
	public static final int MASCULINE = 1;
	public static final int FEMININE  = 2;
	
}

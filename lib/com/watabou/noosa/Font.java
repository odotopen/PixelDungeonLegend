package com.watabou.noosa;

import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.RectF;

import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.utils.PointF;

public class Font extends TextureFilm {
	public static final String SPECIAL_CHAR = 
	"àáâäãèéêëìíîïòóôöõùúûüñçÀÁÂÄÃÈÉÊËÌÍÎÏÒÓÔÖÕÙÚÛÜÑÇº¿¡";

	public static final String LATIN_UPPER = 
	" !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static final String LATIN_FULL = LATIN_UPPER +
	"[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
	
	public static final String CYRILLIC_UPPER =
	"АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";
	
	public static final String CYRILLIC_LOWER =
	"абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
	
	public static final String ALL_CHARS = LATIN_FULL+SPECIAL_CHAR+CYRILLIC_UPPER+CYRILLIC_LOWER;

	public SmartTexture texture;
	
	public float tracking = 0;
	public float baseLine;
	
	public boolean autoUppercase = false;
	
	public float lineHeight;
	
	private boolean endOfRow = false;
	
	HashMap<Object, PointF> glyphShift = new HashMap<Object, PointF>();
	
	protected Font( SmartTexture tx ) {
		super( tx );
		
		texture = tx;
	}
	
	public Font( SmartTexture tx, int width, String chars ) {
		this( tx, width, tx.height, chars );
	}
	
	public Font( SmartTexture tx, int width, int height, String chars ) {
		super( tx );
		
		texture = tx;
		
		autoUppercase = chars.equals( LATIN_UPPER );
		
		int length = chars.length();
		
		float uw = (float)width / tx.width;
		float vh = (float)height / tx.height;
		
		float left = 0;
		float top = 0;
		float bottom = vh;
		
		for (int i=0; i < length; i++) {
			RectF rect = new RectF( left, top, left += uw, bottom );
			add( chars.charAt( i ), rect );
			if (left >= 1) {
				left = 0;
				top = bottom;
				bottom += vh;
			}
		}
		
		lineHeight = baseLine = height;
	}
	
	private int findNextEmptyLine(Bitmap bitmap, int startFrom, int color){
		int width  = bitmap.getWidth();
		int height = bitmap.getHeight();
		
		int nextEmptyLine = startFrom;
		
		for(; nextEmptyLine < height; ++nextEmptyLine){
			boolean lineEmpty = true;
			for(int i = 0;i<width; ++i){
				lineEmpty = (bitmap.getPixel (i, nextEmptyLine ) == color) && lineEmpty;
				if(!lineEmpty){
					break;
				}
			}
			if(lineEmpty){
				break;
			}
		}
		return nextEmptyLine;
	}
	
	private boolean isColumnEmpty(Bitmap bitmap, int x, int sy, int ey, int color){
		for(int j = sy; j < ey; ++j){
			if(bitmap.getPixel(x, j) != color){
				return false;
			}
		}
		return true;
	}

	private int findNextCharColumn(Bitmap bitmap, int sx, int sy, int ey, int color){
		int width = bitmap.getWidth();
		
		int nextEmptyColumn;
		// find first empty column
		for(nextEmptyColumn = sx; nextEmptyColumn < width; ++nextEmptyColumn){
			if(isColumnEmpty(bitmap,nextEmptyColumn, sy, ey, color)){
				break;
			}
		}

		int nextCharColumn;
		
		for(nextCharColumn = nextEmptyColumn; nextCharColumn < width; ++nextCharColumn){
			if(!isColumnEmpty(bitmap,nextCharColumn, sy, ey, color)){
				break;
			}
		}
		
		if(nextCharColumn == width){
			endOfRow = true;
			return nextEmptyColumn - 1;
		}
		
		return nextCharColumn-1;
	}
	
	
	protected void splitBy( Bitmap bitmap, int height, int color, String chars ) {
		
		autoUppercase = chars.equals( LATIN_UPPER );
		int length    = chars.length();
		
		int bWidth  = bitmap.getWidth();
		int bHeight = bitmap.getHeight();
		
		int charsProcessed = 0;
		int lineTop        = 0;
		int lineBottom     = 0;
		
		
		while(lineBottom<bHeight){
			while(lineTop==findNextEmptyLine(bitmap, lineTop, color) && lineTop<bHeight) {
				lineTop++;
			}
			lineBottom = findNextEmptyLine(bitmap, lineTop, color);
			//GLog.w("Empty line: %d", lineBottom);
			int charColumn = 0;
			int charBorder = 0;
			
			endOfRow = false;
			while (! endOfRow){
				if(charsProcessed == length){
					break;
				}
				
				charBorder = findNextCharColumn(bitmap,charColumn+1,lineTop,lineBottom,color);
				
				int glyphBorder = charBorder;
				if(chars.charAt(charsProcessed) != 32) {

					for (;glyphBorder > charColumn + 1; --glyphBorder) {
						if( !isColumnEmpty(bitmap,glyphBorder, lineTop, lineBottom, color)) {
							break;
						}
					}
					glyphBorder++;
				}
				
				//GLog.w("addeded: %d %d %d %d %d",(int)chars.charAt(charsProcessed) ,charColumn, lineTop, glyphBorder, lineBottom);
				add( chars.charAt(charsProcessed), 
					new RectF( (float)(charColumn)/bWidth, 
							   (float)lineTop/bHeight, 
							   (float)(glyphBorder)/bWidth, 
							   (float)lineBottom/bHeight ) );
				++charsProcessed;
				charColumn = charBorder;
			}

			lineTop = lineBottom+1;
		}
		
		lineHeight = baseLine = height( frames.get( chars.charAt( 0 ) ) );
	}
	
	public static Font createEmptyFont( Bitmap bmp) {
		return new Font( TextureCache.get(bmp) );
	}
	
	public static Font colorMarked( Bitmap bmp, int color, String chars ) {
		Font font = new Font( TextureCache.get( bmp ) );
		font.splitBy( bmp, bmp.getHeight(), color, chars );
		return font;
	}
	 
	public static Font colorMarked( Bitmap bmp, int height, int color, String chars ) {
		Font font = new Font( TextureCache.get( bmp ) );
		font.splitBy( bmp, height, color, chars );
		return font;
	}
	
	public void addGlyphShift(char c, PointF shift) {
		glyphShift.put(c,shift);
	}
	
	public RectF get( char ch ){
		RectF rec = super.get( autoUppercase ? Character.toUpperCase(ch) : ch );

		// Fix for fonts without accentuation
		if ((rec == null) && (ch > 126)) {
			char tmp = ch;
			String str = (ch + "")
					.replaceAll("[àáâäãą]", "a")
					.replaceAll("[èéêëę]", "e")
					.replaceAll("[ìíîï]", "i")
					.replaceAll("[òóôöõ]", "o")
					.replaceAll("[ùúûü]", "u")
					.replaceAll("[ÀÁÂÄÃĄ]", "A")
					.replaceAll("[ÈÉÊËĘ]", "E")
					.replaceAll("[ÌÍÎÏ]", "I")
					.replaceAll("[ÒÓÔÖÕ]", "O")
					.replaceAll("[ÙÚÛÜ]", "U")
					.replaceAll("[ÙÚÛÜ]", "U")
					.replaceAll("[ñń]", "n")
					.replaceAll("[ÑŃ]", "N")
					.replaceAll("[ŹŻ]", "Z")
					.replaceAll("[źż]", "z")
					.replaceAll("[ÇĆ]", "C")
					.replaceAll("[çć]", "c")
					.replace("Ł", "L")
					.replace("ł", "l")
					.replace("Ś", "S")
					.replace("ś", "s");

			tmp = str.charAt(0);
			rec = super.get(autoUppercase ? Character.toUpperCase(tmp) : tmp);
		}

		return rec;
	}
}
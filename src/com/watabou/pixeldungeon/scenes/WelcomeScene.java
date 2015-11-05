package com.watabou.pixeldungeon.scenes;

import com.odotopen.pixeldungeon.ml.R;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Chrome;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.ui.Archs;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.ScrollPane;

//TODO: update this class with relevant info as new versions come out.
public class WelcomeScene extends PixelScene {

    private static final String TTL_Welcome = Game.getVar(R.string.Welcome_Title);

    private static final String TXT_Welcome    = Game.getVar(R.string.Welcome_Text);
    private static final String TXT_Welcome_19 = Game.getVar(R.string.Welcome_Text_19);
    private static final String TXT_Welcome_20 = Game.getVar(R.string.Welcome_Text_20);
    
    private static final int GAP = 4;

    @Override
    public void create() {
        super.create();

        Text text    = createMultiline(TXT_Welcome, 8);
        Text text_19 = createMultiline(TXT_Welcome_19, 8);
        Text text_20 = createMultiline(TXT_Welcome_20, 8);
        Text title   = createMultiline(TTL_Welcome, 16);

        int w = Camera.main.width;
        int h = Camera.main.height;

        int pw = w - 10;
        int ph = h - 50;

        title.maxWidth(pw);
        title.measure();

        title.x = align( (w - title.width()) / 2 );
        title.y = align( 8 );
        add( title );

        NinePatch panel = Chrome.get(Chrome.Type.WINDOW);
        panel.size( pw, ph );
        panel.x = (w - pw) / 2;
        panel.y = (h - ph) / 2;
        add( panel );

        ScrollPane list = new ScrollPane( new Component() );
        add( list );
        list.setRect(
                panel.x + panel.marginLeft(),
                panel.y + panel.marginTop(),
                panel.innerWidth(),
                panel.innerHeight());
        list.scrollTo( 0, 0 );

        Component content = list.content();
        content.clear();
        
        text.maxWidth((int) panel.innerWidth());
        text_19.maxWidth((int) panel.innerWidth());
        text_20.maxWidth((int) panel.innerWidth());
        
        text.measure();
        text_19.measure();
        text_20.measure();
      
        text_19.setPos(0, text.height() + GAP);
        text_20.setPos(0,  text.height()+text_19.height() + 2*GAP);
        
        content.add(text);
        content.add(text_19);
        content.add(text_20);

        content.setSize( panel.innerWidth(), text.height() + text_19.height() + text_20.height() + 2*GAP );

        RedButton okay = new RedButton("Okay!") {
            @Override
            protected void onClick() {
                PixelDungeon.version(Game.versionCode);
                Game.switchScene(TitleScene.class);
            }
        };

        okay.setRect((w - pw) / 2, h - 22, pw, 18);
        add(okay);

        Archs archs = new Archs();
        archs.setSize( Camera.main.width, Camera.main.height );
        addToBack( archs );

        fadeIn();
    }
}



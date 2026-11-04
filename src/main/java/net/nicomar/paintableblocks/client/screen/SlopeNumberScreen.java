package net.nicomar.paintableblocks.client.screen;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** Exact sixteenth units: decimal comma/point and fractions, with no silent rounding. */
final class SlopeNumberScreen extends Screen {
    private final Screen parent;private final int value,min,max;private final IntConsumer apply;private EditBox input;private boolean invalid;
    SlopeNumberScreen(Screen parent,String label,int value,int min,int max,IntConsumer apply){super(Component.literal(label));this.parent=parent;this.value=value;this.min=min;this.max=max;this.apply=apply;}
    static int parse(String text,int min,int max){
        var terms=text.strip().replace(',','.').split("/",-1);if(terms.length>2)throw new IllegalArgumentException();
        BigDecimal n=new BigDecimal(terms[0]);if(terms.length==2)n=n.multiply(BigDecimal.valueOf(16)).divide(new BigDecimal(terms[1]),0,RoundingMode.UNNECESSARY);else n=n.multiply(BigDecimal.valueOf(16));
        int result=n.intValueExact();if(result<min||result>max)throw new IllegalArgumentException();return result;
    }
    @Override protected void init(){input=addRenderableWidget(new EditBox(font,width/2-110,height/2-12,220,20,title));input.setMaxLength(32);input.setValue(Double.toString(value/16.0));setInitialFocus(input);
        addRenderableWidget(Button.builder(Component.translatable("button.paintableblocks.confirm"),b->{try{int n=parse(input.getValue(),min,max);apply.accept(n);minecraft.gui.setScreen(parent);}catch(IllegalArgumentException|ArithmeticException e){invalid=true;}}).bounds(width/2-110,height/2+20,106,20).build());
        addRenderableWidget(Button.builder(Component.translatable("button.paintableblocks.cancel"),b->onClose()).bounds(width/2+4,height/2+20,106,20).build());}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float partial){g.fill(0,0,width,height,0xf01a2029);g.text(font,title,width/2-110,height/2-40,0xffffffff);if(invalid)g.text(font,Component.translatable("button.paintableblocks.invalid_number"),width/2-110,height/2+48,0xffff6666);super.extractRenderState(g,x,y,partial);}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}

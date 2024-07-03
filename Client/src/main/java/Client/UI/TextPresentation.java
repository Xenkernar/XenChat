package Client.UI;

public class TextPresentation extends Presentation{
    public TextPresentation(String username,String content, boolean leftAlignment, String time){
        super(username);
        bubble = new TextBubble(content,time);
        putComponent(leftAlignment);
    }

    public TextBubble getBubble() {
        return (TextBubble)bubble;
    }
}

import javax.swing.*;
import java.io.OutputStream;

public class RedirectOutputStream extends OutputStream {
    private final JTextArea textArea;
    private final StringBuilder sb= new StringBuilder();
    private final String title;

    public RedirectOutputStream(JTextArea textArea, String title) {
        this.textArea = textArea;
        this.title= title;
        sb.append(title).append("> ");
    }
    @Override
    public void write(int b) {
        if(b == '\r'){
            return;
        }
        if(b == '\n'){
            final String text= sb +"\n";
            SwingUtilities.invokeLater(() -> textArea.append(text));
            sb.setLength(0);
            sb.append(title).append("> ");
            return;
        }
        sb.append((char) b);
    }
}

package window;

import com.intellij.openapi.project.Project;
import com.intellij.ui.render.LabelBasedRenderer;
import core.beans.Partition;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class CommitWindow extends JFrame {

    private JPanel panel=new JPanel();

    public CommitWindow(Project project, List<Partition> partitionList){
            super("Untangler");

            //JList<Partition> list=new JList<>();
            //list.setListData((Partition[]) partitionList.stream().toArray());
            //panel.add(list);
            this.toFront();
            this.setLocationRelativeTo(null);
            this.setSize(500,300);

            for(Partition p: partitionList){
                JLabel title= new JLabel();
                title.setText("Partizione #"+p.getId()+":");
                title.setBackground(Color.CYAN);
                panel.add(title);
                for(String s: p.getPaths()){
                    JLabel path=new JLabel(s);
                    path.setBackground(Color.RED);
                    path.setForeground(Color.YELLOW);
                    panel.add(path);
                }

            }
            panel.setVisible(true);

            this.setContentPane(panel);

        }


}

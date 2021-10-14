package window;

import com.ibm.icu.text.MessagePattern;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import core.beans.Partition;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Locale;

public class CommitWindow extends JFrame {

    private CommitWindow frame;
    private CommitHandler c;
    private Project project;
    private List<Partition> list;
    private JButton buttonPositive;
    private JButton buttonNegative;

    public CommitWindow(Project project, List<Partition> partitionList, CheckinHandlerFactory c){
        super("Untangler | Detection");

        this.setSize(700,500);
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        this.toFront();

        this.frame=this;
        this.project=project;
        this.list=partitionList;
        this.c= (CommitHandler) c;
        this.setLayout(new BorderLayout());
        this.add(mainPanel(),BorderLayout.CENTER);

    }

    private JPanel mainPanel(){
        JPanel panel=new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(messagePanel(),BorderLayout.CENTER);
        panel.add(buttonPanel(),BorderLayout.SOUTH);
        Border margin = new EmptyBorder(10, 20, 10, 20);
        panel.setBorder(margin);
        return panel;

    }

    private JPanel messagePanel(){
        JPanel panel=new JPanel(new BorderLayout());
        JLabel label=new JLabel(("Sono stati rilevati "+list.size()+" tangled Changes.").toUpperCase(Locale.ROOT));
        label.setFont(new Font(Font.SANS_SERIF,Font.BOLD,20));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label,BorderLayout.NORTH);
        panel.add(showPartitions(),BorderLayout.CENTER);
        return panel;
    }


    private JScrollPane showPartitions(){
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0,1));
        Border margin = new EmptyBorder(10, 20, 10, 20);
        panel.setBorder(margin);
        for(int i=0; i<list.size(); i++){
            Partition p=list.get(i);
            JLabel campo = new JLabel("PARTITION ID:"+p.getId()+"");
            panel.add(campo);
            for(String path: p.getPaths()){
                JLabel item = new JLabel("path: "+path);
                panel.add(item);
            }

            panel.add(new MyLine());
        }
        JScrollPane scroll= new JScrollPane(panel);
        scroll.setBorder(new EmptyBorder(0,0,0,0));
        return scroll;
    }

    private JPanel buttonPanel(){

        JPanel panel=new JPanel();
        buttonPositive=new JButton("Untangle");
        buttonNegative=new JButton("Ignore and continue");
        buttonPositive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                c.doUntagledCommit();
                frame.dispose();
                JOptionPane.showMessageDialog(frame,"UNTANGLED COMMIT COMPLETE!");
            }
        });
        buttonNegative.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    c.doTangledCommit();
                } catch (GitAPIException gitAPIException) {
                    gitAPIException.printStackTrace();
                }

                frame.dispose();
                JOptionPane.showMessageDialog(frame,"TANGLED COMMIT COMPLETE!");

            }
        });
        panel.add(buttonPositive);
        panel.add(buttonNegative);

        return panel;
    }



}

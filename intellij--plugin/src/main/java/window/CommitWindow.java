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
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommitWindow extends JFrame {

    private final CommitWindow frame;
    private final CommitHandler c;
    private final Project project;
    private final List<Partition> list;
    private JButton buttonPositive;
    private JButton buttonNegative;
    private final ArrayList<JTextArea> textAreaList;

    public CommitWindow(Project project, List<Partition> partitionList, CheckinHandlerFactory c){
        //super("Untangler | Detection");

        //this.setSize(700,500);
        //this.setVisible(true);
        //this.setLocationRelativeTo(null);
        //this.toFront();

        this.frame=this;
        this.project=project;
        this.list=partitionList;
        this.c= (CommitHandler) c;
        textAreaList=new ArrayList<>();

        //this.setLayout(new BorderLayout());
        //this.add(mainPanel(),BorderLayout.CENTER);

    }

    public JPanel mainPanel(){
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
        panel.setLayout(new GridLayout(0,3));
        Border margin = new EmptyBorder(10, 20, 10, 20);
        panel.setBorder(margin);

        for(int i=0; i<list.size(); i++){
            JPanel innerPanel=new JPanel(new GridLayout(0,1));

            Partition p=list.get(i);
            //JLabel campo = new JLabel("PARTITION ID:"+p.getId()+"");
            TitledBorder titledBorder=BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),"PARTITION ID:"+p.getId()+"");
            titledBorder.setTitleJustification(TitledBorder.ABOVE_TOP);
            innerPanel.setBorder(titledBorder);
            //innerPanel.add(campo);
            for(String path: p.getPaths()){
                JLabel item = new JLabel("  path: "+path);
                innerPanel.add(item);
            }
            JTextArea textArea=new JTextArea("UNTANGLED COMMIT #"+p.getId()+" -- "+ new Date());
            textArea.setEditable(true);
            textAreaList.add(textArea);
            JPanel textAreaPanel=new JPanel();
            textAreaPanel.add(new JLabel("Insert commit message:"));
            textAreaPanel.add(textArea);
            innerPanel.add(textAreaPanel);
            //innerPanel.add(new MyLine());
            panel.add(innerPanel);
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

                ArrayList<String> messages=new ArrayList<>();
                for(JTextArea t : textAreaList){
                    messages.add(t.getText());
                }
                c.setMessages(messages);
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

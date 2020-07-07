/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author 15002
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JTextPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class UIPrimDisTable extends JPanel {

    private final JTextPane edgeNames = new JTextPane();
    private final JTextPane edgeDistances = new JTextPane();
    private final ConcurrentSkipListSet<STEdge> edgeTable;
    private final JPanel titlePanel = new JPanel();
    private final JPanel contentPanel = new JPanel();
    private final STGraph data;
    private final CopyOnWriteArrayList<STNode> nodes;
    private final Timer tableThread = new Timer(5, (ActionEvent evt) -> {
        refreshTable();
    });

    public UIPrimDisTable(STGraph tree) {
        setPreferredSize(new Dimension(400, 600));
        DefaultCaret caret = (DefaultCaret) edgeNames.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        caret = (DefaultCaret) edgeDistances.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        data = tree;
        nodes = data.getNodes();
        edgeNames.setEditable(false);
        edgeDistances.setEditable(false);
        edgeNames.setBackground(new Color(238, 238, 238));
        edgeDistances.setBackground(new Color(238, 238, 238));
        JScrollPane scrollLeft = new JScrollPane(edgeNames, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JScrollPane scrollRight = new JScrollPane(edgeDistances, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollLeft.getHorizontalScrollBar().setModel(scrollRight.getHorizontalScrollBar().getModel());
        scrollLeft.getVerticalScrollBar().setModel(scrollRight.getVerticalScrollBar().getModel());
        contentPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        setLayout(new BorderLayout());
        titlePanel.add(new JLabel("Candidates", JLabel.CENTER));
        titlePanel.add(new JLabel("Length     ", JLabel.CENTER));
        titlePanel.setPreferredSize(new Dimension(400, 34));
        contentPanel.add(scrollLeft);
        contentPanel.add(scrollRight);
        titlePanel.setLayout(new GridLayout(1, 2));
        contentPanel.setLayout(new GridLayout(1, 2));
        contentPanel.setBorder(new EmptyBorder(0, 0, 53, 0));
        add(titlePanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        edgeTable = new ConcurrentSkipListSet<STEdge>();
    }

    public void refreshTable() {
        empty();
        if (!nodes.isEmpty()) {
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i).isVisited()) {
                    for (int j = 0; j < nodes.size(); j++) {
                        if (j != i && !(nodes.get(i).isVisited() && nodes.get(j).isVisited())) {
                            STEdge edge = new STEdge(nodes.get(i), nodes.get(j), false);
                            if (!edgeTable.contains(edge)) {
                                edgeTable.add(edge);
                            }
                        }
                    }
                }
            }
        }
        while (!edgeTable.isEmpty()) {
            try {
                StyledDocument nameDoc = (StyledDocument) edgeNames.getDocument();
                StyledDocument disDoc = (StyledDocument) edgeDistances.getDocument();
                STEdge edge = edgeTable.pollFirst();
                nameDoc.insertString(nameDoc.getLength(), " " + edge.toTableString() + " " + "\n", null);
                disDoc.insertString(disDoc.getLength(), " " + edge.length() + " " + "\n", null);
                SimpleAttributeSet center = new SimpleAttributeSet();
                StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
                nameDoc.setParagraphAttributes(0, nameDoc.getLength(), center, false);
                disDoc.setParagraphAttributes(0, disDoc.getLength(), center, false);
            } catch (BadLocationException ex) {
                Logger.getLogger(UIPrimDisTable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void empty() {
        edgeTable.clear();
        edgeNames.setText("");
        edgeDistances.setText("");
    }

    public void refresh() {
        tableThread.setRepeats(false);
        tableThread.start();
    }

    public void stop() {
        tableThread.stop();
    }

    public void highlight() {
        highlight(edgeNames);
        highlight(edgeDistances);
    }

    private void highlight(JTextPane pane) {
        if (!pane.getText().isEmpty()) {
            Highlighter hilit = new DefaultHighlighter();
            Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);
            pane.setHighlighter(hilit);
            try {
                hilit.addHighlight(0, pane.getText().indexOf("\n"), painter);
            } catch (BadLocationException ex) {
                Logger.getLogger(UIPrimDisTable.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println(pane.getText().indexOf("\n"));
            }
        }
    }
}

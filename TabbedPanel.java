import java.io.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.nio.file.*;
import java.nio.charset.Charset;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.CommonTokenStream;

class TabbedPanel extends JFrame
{
    private JTextArea areaGrammar = new JTextArea(20,120);
    public JTextArea areaTest = new JTextArea(20,120);
    public JScrollPane scrollError = null;
    public JTabbedPane tabbedPane;
    private JTextArea areaError = new JTextArea(20,120);
    private JFileChooser dialog = new JFileChooser(System.getProperty("user.dir"));
    private String currentFile = "Untitled";
    private boolean changed = false;
    private Path file = Paths.get("ErrorLog_Syntax.log");
    private List<String> errors;

    JPanel treePanel = new JPanel();

    JScrollPane scrollTreePanel = new JScrollPane(
        treePanel,
        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
    );

    Action text2Tree = new AbstractAction("Compile") {
        public void actionPerformed(ActionEvent e) {
            readToTree();
        }
    };

    public TabbedPanel()
    {
        add(scrollTreePanel, BorderLayout.CENTER);
        scrollTreePanel.setVisible(true);

        // Test Editor
        areaTest.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollTest = new JScrollPane(
            areaTest,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
        );
        add(scrollTest, BorderLayout.CENTER);
        scrollTest.setVisible(true);

        // Error Viewr
        areaError.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaError.setEditable(false);
        scrollError = new JScrollPane(
            areaError,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
        );
        add(scrollError, BorderLayout.CENTER);
        scrollError.setVisible(true);

        // Buttons
        JToolBar tool = new JToolBar();
        add(tool, BorderLayout.WEST);
        tool.addSeparator();

        JButton antlr = tool.add(text2Tree);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();

        setTitle(currentFile);
        setVisible(true);

        tabbedPane = new JTabbedPane();
        Container contentPane = getContentPane();

        contentPane.add(tabbedPane,BorderLayout.CENTER);

        tabbedPane.addTab("Input Text to Test", scrollTest);
        tabbedPane.addTab("Tree", scrollTreePanel);
        tabbedPane.addTab("Errors", scrollError);
    }

    public Integer readToTree()
    {
        // ANTLR Tree
        ANTLRInputStream input = new ANTLRInputStream(areaTest.getText());

        BayesLexer lexer = new BayesLexer(input);

        BaseErrorListener miErrorListener = new ThrowingErrorListener();

        // Add custom error handlers
        lexer.removeErrorListeners();
        lexer.addErrorListener(miErrorListener);

        TokenStream tokenStream = new CommonTokenStream(lexer);

        BayesParser parser = new BayesParser(tokenStream);

        // Add custom error handlers
        parser.removeErrorListeners();
        parser.addErrorListener(miErrorListener);

        ParseTree tree = parser.program();
        TreeViewer treeViewer = new TreeViewer(
            Arrays.asList(parser.getRuleNames()),
            tree
        );
        treeViewer.setSize(700, 700);

        // De no poner esto, hace un arbol justo a la par
        treePanel.removeAll();
        treePanel.add(treeViewer);

        // Read and write errors.
        try {
            // Este es el visitador del análisis semántico
           

            areaError.setText("");
            errors = Files.readAllLines(file, Charset.forName("UTF-8"));
            Files.deleteIfExists(file);

            for (int i = 0; i < errors.size(); i++) {
                areaError.append("(" + (i + 1) + "): " + errors.get(i) + "\n");
            }
            
            

            return 1;
        } catch ( IOException e ) {
            areaError.setText("No syntactic errors \n\n");
            
            MyVisitor visitor = new MyVisitor();
    		visitor.visit(tree);
    		
    		boolean stopExecuting = false;
    		String descriptionComplete = "";
    		for (Node node: visitor.nodes){
            	int result = (int)Math.pow(2, node.getDependencies().size());
            	if(node.getProbabilities().size() != result){
            		descriptionComplete = descriptionComplete +
            				String.format("Node: %s is missing information \n"
            				+ "Description: \n"
            				+ "%s", node.getIdentifier(),node.toString());
            		stopExecuting = true;
            	}
            	else{
            		descriptionComplete = descriptionComplete +
            				String.format("Node: %s has enough information \n", node.getIdentifier());
            	}
            }
    		areaError.append("Bayesian Network Semantics: \n");
    		areaError.append(descriptionComplete);
    		System.out.println(visitor.nodes);
    		if (stopExecuting == false){
    			String jointProbability = "";
	            
	            for (Node node: visitor.nodes){
	            	String relations = "";
	            	if (node.getDependencies().size()>0){
	            		int setCounter = 0;
	            		for (String dependency:node.getDependencies()){
	            			
	            			if (setCounter == node.getDependencies().size()-1){
	            				relations = relations + generateJoint(dependency, "");
	            			}else{
	            				relations = relations + generateJoint(dependency, ",");
	            			}
	            			setCounter += 1;
	            		}
	            		jointProbability = jointProbability +( String.format("P(%s|%s)", node.getIdentifier(),relations));
	            	}else{
	            		jointProbability = jointProbability +( String.format("P(%s)", node.getIdentifier()));
	            	}
	            }
	            areaError.append("\nJoint Probability: \n");
	            areaError.append(jointProbability);
	            areaError.append("\n\nFactors: \n");
	            areaError.append(generateFactors(visitor.nodes));
    		}
    	

            return 1;
        }
    }
    
    private void generarArhivoCodigoIntermedio(ArrayList<String> codigoIntermedioArrayList)
    {
    	Path file = Paths.get("intermediate_output.txt");

    	try {
    		Files.write(file, codigoIntermedioArrayList, Charset.forName("UTF-8"));
        } catch (IOException e) {
            System.err.println("Something is wrong.");
        }
    }
    
    private String generateJoint(String dependency, String coma){
    	return (String.format("%s%s", dependency,coma)); 
    }
    
    private String generateFactors(ArrayList<Node> nodes){
    	String factors = "";
    	for(Node node: nodes){
    		node.getFactorAsString();
    		factors = factors + String.format("Node: %s \nFactor: %s\n", node.getIdentifier(),node.factor.toString());
    	}
    	return factors;
    }
}

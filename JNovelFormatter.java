import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class JNovelFormatter extends JFrame {
	private JPanel panel = new JPanel();
	
	private JButton btnFile;
	private JButton btnOutDir;
	private JButton btnFormat;
	
	private JTextField tfDir;
	private JTextField tfOutDir;
	
	private final String[] strEncodings = {"x-SJIS_0213", "UTF-8", "UTF-16"};
	private JComboBox<String> cbEncoding;
	
	private JCheckBox chkbBookmark;
	
	public JNovelFormatter() {
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		panel.add(new JLabel("Novel File or Directory:"));
		btnFile = new JButton("File...");
		tfDir = new JTextField();

		btnFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new java.io.File("."));
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Aozora Bunko text file", "txt");
				chooser.setFileFilter(filter);
				int returnVal = chooser.showOpenDialog(panel);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					tfDir.setText(chooser.getSelectedFile().getCanonicalPath());	
				} catch(Exception x) {
					JOptionPane.showMessageDialog(null, "Error");
					x.printStackTrace();
				}
    		}
			}
		});

		panel.add(btnFile);
		panel.add(tfDir);
		
		panel.add(new JLabel("Output Directory:"));
		btnOutDir = new JButton("Out Dir...");
		tfOutDir = new JTextField();

		btnOutDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = chooser.showOpenDialog(panel);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					try {
						tfOutDir.setText(chooser.getSelectedFile().getCanonicalPath());	
					} catch(Exception x) {
						JOptionPane.showMessageDialog(panel, "Error");
						x.printStackTrace();
					}
				}
			}
		});

		panel.add(btnOutDir);
		panel.add(tfOutDir);
		
		panel.add(new JLabel("Encoding:"));
		cbEncoding = new JComboBox<>(strEncodings);
		panel.add(cbEncoding);
		
		chkbBookmark = new JCheckBox("Add a bookmark anchor to every '.'");
		panel.add(chkbBookmark);
		
		btnFormat = new JButton("Format!");
		btnFormat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String encoding = cbEncoding.getSelectedItem().toString();
				
				String fileName = tfDir.getText();
				// System.out.println(fileName);
				Path input = Paths.get(fileName);

				try {
					java.util.List<String> lines = Files.readAllLines(input, Charset.forName(encoding));
					StringBuilder textsb = new StringBuilder();
					boolean skip = false;
					for (String line : lines) {
						if (!skip) {
							if (line.startsWith("-")) 
								skip = true;
							else
								textsb.append(line).append("\n");
						} else {
							if (line.startsWith("-")) 
								skip = false;
						}
					}
					String text = textsb.toString();
					AozoraBunkoRuby abp = new AozoraBunkoRuby(text);
					
					StringBuilder sb = new StringBuilder();
					sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
					sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
					sb.append("<head>");
					sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
					sb.append("<link rel='stylesheet' type='text/css' href='jnf_style.css' />");
					sb.append("<script defer>");
					sb.append("window.addEventListener(\"wheel\", function (event) { document.documentElement.scrollLeft -= event.deltaY; });");
					sb.append("</script>");
					sb.append("</head>");
					sb.append("<body>");
					
					if (chkbBookmark.isSelected())
						sb.append(abp.bookmark(abp.parse()));
					else
						sb.append(abp.parse());
					
					sb.append("</body>");
					sb.append("</html>");
					
					String outfile = input.getFileName().toString();
					outfile = (outfile.indexOf('.') > 0) ? outfile.substring(0, outfile.indexOf('.')) : outfile;
					Path output = Paths.get(tfOutDir.getText() + "/" + outfile + ".html");

					try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
						writer.append(sb.toString());
						JOptionPane.showMessageDialog(panel, "Done");
					} catch (IOException x) {
						JOptionPane.showMessageDialog(panel, "Failed to write file: " + output.toString());
						x.printStackTrace();
					}

					// Copy the CSS file to the output directory
					Path cssSource = Paths.get("jnf_style.css");
					Path cssDestination = Paths.get(tfOutDir.getText() + "/jnf_style.css");
					if (!Files.exists(cssDestination)) {
						Files.copy(cssSource, cssDestination, StandardCopyOption.REPLACE_EXISTING);
					}

				} catch (IOException x) {
					JOptionPane.showMessageDialog(panel, "Incorrect encoding");
					x.printStackTrace();
				}
			}
		});
		
		panel.add(btnFormat);
		
		add(panel);
		setSize(350,350);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	public static void main(String[] args) {
		new JNovelFormatter();
	}
}

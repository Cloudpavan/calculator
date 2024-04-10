import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class jCalculatorGUI extends JFrame implements ActionListener {
    private JTextField display;
    private String inputString = "";

    public jCalculatorGUI() {
        setTitle("Calculator");
        setSize(300, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        display = new JTextField();
        display.setEditable(false);
        add(display, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(5, 4));
        String[] buttonLabels = {
            "7", "8", "9", "/",
            "4", "5", "6", "*",
            "1", "2", "3", "-",
            "0", ".", "=", "+",
            "sin", "cos", "tan", "C"
        };

        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.addActionListener(this);
            buttonPanel.add(button);
        }

        add(buttonPanel, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals("=")) {
            calculate();
        } else if (command.equals("C")) {
            clear();
        } else if (command.equals("sin") || command.equals("cos") || command.equals("tan")) {
            performTrigonometric(command);
        } else {
            inputString += command;
            display.setText(inputString);
        }
    }

    private void calculate() {
        try {
            String result = String.valueOf(eval(inputString));
            display.setText(result);
            inputString = result;
        } catch (Exception ex) {
            display.setText("Error");
            inputString = "";
        }
    }

    private void clear() {
        inputString = "";
        display.setText("");
    }

    private void performTrigonometric(String operation) {
        double value;
        try {
            value = Double.parseDouble(display.getText());
        } catch (NumberFormatException ex) {
            display.setText("Invalid input");
            return;
        }

        double result = 0;
        switch (operation) {
            case "sin":
                result = Math.sin(value);
                break;
            case "cos":
                result = Math.cos(value);
                break;
            case "tan":
                result = Math.tan(value);
                break;
        }

        display.setText(String.valueOf(result));
        inputString = "";
    }

    // Method to evaluate the expression
    private double eval(String expression) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus
                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expression.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = expression.substring(startPos, this.pos);
                    x = parseFactor();
                    switch (func) {
                        case "sin":
                            x = Math.sin(Math.toRadians(x));
                            break;
                        case "cos":
                            x = Math.cos(Math.toRadians(x));
                            break;
                        case "tan":
                            x = Math.tan(Math.toRadians(x));
                            break;
                        default:
                            throw new RuntimeException("Unknown function: " + func);
                    }
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new jCalculatorGUI().setVisible(true));
    }
}

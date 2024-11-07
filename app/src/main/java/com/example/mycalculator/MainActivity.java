package com.example.mycalculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mycalculator.databinding.ActivityMainBinding;

import java.util.Stack;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private boolean resultCalculated = false;
    private boolean errorState = false;
    String f1 = "";
    String f2 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btn0.setOnClickListener(numberClickListener);
        binding.btn1.setOnClickListener(numberClickListener);
        binding.btn2.setOnClickListener(numberClickListener);
        binding.btn3.setOnClickListener(numberClickListener);
        binding.btn4.setOnClickListener(numberClickListener);
        binding.btn5.setOnClickListener(numberClickListener);
        binding.btn6.setOnClickListener(numberClickListener);
        binding.btn7.setOnClickListener(numberClickListener);
        binding.btn8.setOnClickListener(numberClickListener);
        binding.btn9.setOnClickListener(numberClickListener);
        binding.btnDot.setOnClickListener(numberClickListener);

        binding.btnPlus.setOnClickListener(operationClickListener);
        binding.btnMinus.setOnClickListener(operationClickListener);
        binding.btnMultiply.setOnClickListener(operationClickListener);
        binding.btnDivide.setOnClickListener(operationClickListener);

        binding.btnEquals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (errorState) {
                    resetCalculator();
                    return;
                }

                String resultText = binding.result.getText().toString();
                String processText = binding.process.getText().toString();

                if (!containsOperator(resultText)) {
                    binding.process.setText(processText + "0");
                    updateProcess(resultText);
                } else {
                    try {
                        double result = evaluateExpression(resultText);
                        String formattedResult = formatResult(result);
                        updateProcess(formattedResult);
                    } catch (ArithmeticException e) {
                        binding.result.setText("ERROR");
                        binding.process.setText("ERROR");
                        f1 = "";
                        f2 = "";
                        errorState = true;
                    }
                }
                resultCalculated = true;
            }
        });

        binding.btnC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetCalculator();
            }
        });

        binding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (errorState) {
                    resetCalculator();
                    return;
                }

                String resultText = binding.result.getText().toString();
                String processText=binding.process.getText().toString();
                binding.process.setText(processText.substring(0, processText.length() - 1));
                if (resultText.length() > 1) {
                    binding.result.setText(resultText.substring(0, resultText.length() - 1));
                } else if (resultText.length() == 1 && !resultText.equals("0")) {
                    binding.result.setText("0");
                }

            }
        });

        binding.btnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (errorState) {
                    resetCalculator();
                    return;
                }

                String resultText = binding.result.getText().toString();
                if (!resultText.isEmpty()) {
                    String[] tokens = resultText.split("(?<=[-+*/])|(?=[-+*/])");
                    String lastToken = tokens[tokens.length - 1];
                    if (lastToken.matches("-?\\d+(\\.\\d+)?")) {
                        double value = Double.parseDouble(lastToken);
                        value *= -1;
                        tokens[tokens.length - 1] = formatResult(value);
                        String updatedResult = String.join("", tokens);
                        binding.result.setText(updatedResult);

                        // Cập nhật chuỗi process tương ứng
                        String processText = binding.process.getText().toString();
                        processText = processText.substring(0, processText.lastIndexOf(lastToken)) + formatResult(value);
                        binding.process.setText(processText);
                    }
                }
            }
        });
    }

    private final View.OnClickListener numberClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (errorState) {
                resetCalculator();
            }

            Button button = (Button) view;
            String buttonText = button.getText().toString();

            if (resultCalculated) {
                binding.result.setText(buttonText);
                binding.process.append(" " + buttonText);
                resultCalculated = false;
            } else {
                binding.result.append(buttonText);
                binding.process.append(buttonText);
            }
        }
    };

    private final View.OnClickListener operationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (errorState) {
                resetCalculator();
            }

            Button button = (Button) view;
            String buttonText = button.getText().toString();
            String resultText = binding.result.getText().toString();

            if (resultCalculated) {
                binding.result.append(buttonText);
                binding.process.setText(f2 + " " + resultText + buttonText);
                resultCalculated = false;
            } else {
                if (containsOperator(resultText)) {
                    try {
                        double result = evaluateExpression(resultText);
                        String formattedResult = formatResult(result);
                        updateProcess(formattedResult);
                        binding.result.setText(formattedResult + buttonText);
                        binding.process.setText(f2 + " " + binding.result.getText());
                    } catch (ArithmeticException e) {
                        binding.result.setText("ERROR");
                        binding.process.setText("ERROR");
                        errorState = true;
                    }
                } else {
                    binding.result.append(buttonText);
                    binding.process.append(buttonText);
                }
            }
        }
    };

    private boolean containsOperator(String text) {
        return text.contains("+") || text.contains("-") || text.contains("*") || text.contains("/");
    }

    private double evaluateExpression(String expression) throws ArithmeticException {
        if (expression.startsWith("-")) {
            expression = "0" + expression;
        }

        Stack<Double> numbers = new Stack<>();
        Stack<Character> operators = new Stack<>();
        StringBuilder number = new StringBuilder();

        for (char c : expression.toCharArray()) {
            if (Character.isDigit(c) || c == '.') {
                number.append(c);
            } else {
                numbers.push(Double.parseDouble(number.toString()));
                number.setLength(0);
                while (!operators.isEmpty() && precedence(c) <= precedence(operators.peek())) {
                    double b = numbers.pop();
                    double a = numbers.pop();
                    char op = operators.pop();
                    numbers.push(applyOperation(a, b, op));
                }
                operators.push(c);
            }
        }
        numbers.push(Double.parseDouble(number.toString()));

        while (!operators.isEmpty()) {
            double b = numbers.pop();
            double a = numbers.pop();
            char op = operators.pop();
            numbers.push(applyOperation(a, b, op));
        }

        return numbers.pop();
    }

    private int precedence(char operator) {
        switch (operator) {
            case '+':
            case '-':
                return 1;
            case '*':
            case '/':
                return 2;
        }
        return -1;
    }

    private double applyOperation(double a, double b, char operator) throws ArithmeticException {
        switch (operator) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) throw new ArithmeticException("You cannot divide by 0");
                return a / b;
        }
        return 0;
    }

    private String formatResult(double result) {
        if (result == (long) result) {
            return String.format("%d", (long) result);
        } else {
            return String.format("%s", result);
        }
    }

    public void updateProcess(String s) {
        f1 = f2;
        f2 = binding.result.getText().toString() + "=" + s;
        binding.process.setText(f1 + " " + f2);
        binding.result.setText(s);
    }

    private void resetCalculator() {
        binding.result.setText("");
        binding.process.setText("");
        resultCalculated = false;
        errorState = false;
        f1 = "";
        f2 = "";
    }
}

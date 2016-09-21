/*
    Thompson.java
        Compiler for Regular Expressions to Non-Deterministic Finite 
        Automata (NFA). Currently set to only work on regular expressions 
        with the alphabet of ['a','z'].
        
        This works as a Left to Right comiler, giving precedence to the left
        characters over the right. Of course this is the weakest form of 
        precedence in the compiler, after the operator precedence.

        Operator Syntax:
                '|' for union (lowest precedence)
                'ab' for some elements a and b to concat a and b. ie. 
                    concatentation done w/o operator. (middle precedence)
                '*' for kleene star (highest precedence)
                '(' & ')' for controlling precedence of operators
    @author Derek S. Prijatelj
*/
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Stack;

public class Thompson{
    /*
        Trans - object is used as a tuple of 3 items to depict transitions
            (state from, symbol of tranistion path, state to)
    */
    public static class Trans{
        public int state_from, state_to;
        public char trans_symbol;

        public Trans(int v1, int v2, char sym){
            this.state_from = v1;
            this.state_to = v2;
            this.trans_symbol = sym;
        }
    }

    /*
        NFA - serves as the graph that represents the Non-Deterministic
            Finite Automata. Will use this to better combine the states.
    */
    public static class NFA{
        public ArrayList <Integer> states;
        public ArrayList <Trans> transitions;
        public int final_state;
        
        public NFA(){
            this.states = new ArrayList <Integer> ();
            this.transitions = new ArrayList <Trans> ();
            this.final_state = 0;
        }
        public NFA(int size){
            this.states = new ArrayList <Integer> ();
            this.transitions = new ArrayList <Trans> ();
            this.final_state = 0;
            this.setStateSize(size);
        }
        public NFA(char c){
            this.states = new ArrayList<Integer> ();
            this.transitions = new ArrayList <Trans> ();
            this.setStateSize(2);
            this.final_state = 1;
            this.transitions.add(new Trans(0, 1, c));
        }

        public void setStateSize(int size){
            for (int i = 0; i < size; i++)
                this.states.add(i);
        }

        public void display(){
            for (Trans t: transitions){
                System.out.println("("+ t.state_from +", "+ t.trans_symbol +
                    ", "+ t.state_to +")");
            }    
        }
    }

    /*
        kleene() - Highest Precedence regular expression operator. Thompson
            algoritm for kleene star.
    */
    public static NFA kleene(NFA n){
        NFA result = new NFA(n.states.size()+2);
        result.transitions.add(new Trans(0, 1, 'E')); // new trans for q0

        // copy existing transisitons
        for (Trans t: n.transitions){
            result.transitions.add(new Trans(t.state_from + 1,
            t.state_to + 1, t.trans_symbol));
        }
        
        // add empty transition from final n state to new final state.
        result.transitions.add(new Trans(n.states.size(), 
            n.states.size() + 1, 'E'));
        
        // Loop back from last state of n to initial state of n.
        result.transitions.add(new Trans(n.states.size(), 1, 'E'));

        // Add empty transition from new initial state to new final state.
        result.transitions.add(new Trans(0, n.states.size() + 1, 'E'));

        result.final_state = n.states.size() + 1;
        return result;
    }

    /*
        concat() - Thompson algorithm for concatenation. Middle Precedence.
    */
    public static NFA concat(NFA n, NFA m){
        ///*
        m.states.remove(0); // delete m's initial state

        // copy NFA m's transitions to n, and handles connecting n & m
        for (Trans t: m.transitions){
            n.transitions.add(new Trans(t.state_from + n.states.size()-1,
                t.state_to + n.states.size() - 1, t.trans_symbol));
        }

        // take m and combine to n after erasing inital m state
        for (Integer s: m.states){
            n.states.add(s + n.states.size() + 1);
        }
        
        n.final_state = n.states.size() + m.states.size() - 2;
        return n;
        //*/
        /* ~~~ Makes new NFA, rather than mod n. I believe my above
            sacrifice trades non-changed original for speed. Not much gain
            though. And could be implemented in the other functions.
        
        NFA result = new NFA(n.states.size() + m.states.size());

        // copy NFA n's transitions to result
        for (Trans t: n.transitions){
            result.transitions.add(new Trans(t.state_from, t.state_to,
                t.trans_symbol));
        }

        // empty transition from final state of n to beginning state of m
        result.transitions.add(new Trans(n.final_state, n.states.size(), 
            'E'));

        // copy NFA m's transitions to result
        for (Trans t: m.transitions){
            result.transitions.add(new Trans(t.state_from + n.states.size(),
                t.state_to + n.states.size(), t.trans_symbol));
        }
        
        result.final_state = n.final_state + m.final_state - 1;
        return result;
        */
    }
    
    /*
        union() - Lowest Precedence regular expression operator. Thompson
            algorithm for union (or). 
    */
    public static NFA union(NFA n, NFA m){
        NFA result = new NFA(n.states.size() + m.states.size() + 2);

        // the branching of q0 to beginning of n
        result.transitions.add(new Trans(0, 1, 'E'));
        
        // copy existing transisitons of n
        for (Trans t: n.transitions){
            result.transitions.add(new Trans(t.state_from + 1,
            t.state_to + 1, t.trans_symbol));
        }
        
        // transition from last n to final state
        result.transitions.add(new Trans(n.states.size(),
            n.states.size() + m.states.size() + 1, 'E'));

        // the branching of q0 to beginning of m
        result.transitions.add(new Trans(0, n.states.size() + 1, 'E'));

        // copy existing transisitons of m
        for (Trans t: m.transitions){
            result.transitions.add(new Trans(t.state_from + n.states.size()                 + 1, t.state_to + n.states.size() + 1, t.trans_symbol));
        }
        
        // transition from last m to final state
        result.transitions.add(new Trans(m.states.size() + n.states.size(),
            n.states.size() + m.states.size() + 1, 'E'));
       
        // 2 new states and shifted m to avoid repetition of last n & 1st m
        result.final_state = n.states.size() + m.states.size() + 1;
        return result;
    }

    /*
        Recursive Descent Parser: Recursion To Parse the String.
            I have already written a Recursive Descent Parser, and so I am 
            giving stacks a go instead. This code snippet is the basic 
            structure of my functions if I were to do RDP.
    
    // <uni> := <concat> { |<concat> }
    public static NFA uni(String regex, NFA n){
        
    }
    // <conact> := <kleene> { .<kleene> }
    public static NFA concatenation(String regex, NFA n){
        
    }
    // <kleene> := <element> | <element>*
    public static NFA kleeneStar(String regex, NFA n){
        
    }
    // <element> := letter | E | ( <uni> )
    public static NFA element(String regex, NFA n){
        if (regex.charAt(0) == '('){
            uni(regex.substring(1),n);
            if(!regex.charAt(0) == ')'){
                System.out.println("Missing End Paranthesis.");
                System.exit(1);
            }

        }
    }
    */

    // simplify the repeated boolean condition checks
    public static boolean alpha(char c){ return c >= 'a' && c <= 'z';}
    public static boolean alphabet(char c){ return alpha(c) || c == 'E';}
    public static boolean regexOperator(char c){
        return c == '(' || c == ')' || c == '*' || c == '|';
    }
    public static boolean validRegExChar(char c){
        return alphabet(c) || regexOperator(c);
    }
    // validRegEx() - checks if given string is a valid regular expression.
    public static boolean validRegEx(String regex){
        if (regex.isEmpty())
            return false;
        for (char c: regex.toCharArray())
            if (!validRegExChar(c))
                return false;
        return true;
    }

    /*
        compile() - compile given regualr expression into a NFA using 
            Thompson Construction Algorithm. Will implement typical compiler
            stack model to simplify processing the string. This gives 
            descending precedence to characters on the right.
    */
    public static NFA compile(String regex){
        if (!validRegEx(regex)){
            System.out.println("Invalid Regular Expression Input.");
            return new NFA(); // empty NFA if invalid regex
        }
        
        Stack <Character> operators = new Stack <Character> ();
        Stack <NFA> operands = new Stack <NFA> ();
        Stack <NFA> concat_stack = new Stack <NFA> ();
        boolean ccflag = false; // concat flag
        char op, c; // current character of string
        int para_count = 0;
        NFA nfa1, nfa2;

        for (int i = 0; i < regex.length(); i++){
            c = regex.charAt(i);
            if (alphabet(c)){
                operands.push(new NFA(c));
                if (ccflag){ // concat this w/ previous
                    operators.push('.'); // '.' used to represent concat.
                }
                else
                    ccflag = true;
            }
            else{
                if (c == ')'){
                    ccflag = false;
                    if (para_count == 0){
                        System.out.println("Error: More end paranthesis "+
                            "than beginning paranthesis");
                        System.exit(1);
                    }
                    else{ para_count--;}
                    // process stuff on stack till '('
                    while (!operators.empty() && operators.peek() != '('){
                        op = operators.pop();
                        if (op == '.'){
                            nfa2 = operands.pop();
                            nfa1 = operands.pop();
                            operands.push(concat(nfa1, nfa2));
                        }
                        else if (op == '|'){
                            nfa2 = operands.pop();
                            
                            if(!operators.empty() && 
                                operators.peek() == '.'){
                                
                                concat_stack.push(operands.pop());
                                while (!operators.empty() && 
                                    operators.peek() == '.'){
                                    
                                    concat_stack.push(operands.pop());
                                    operators.pop();
                                }
                                nfa1 = concat(concat_stack.pop(),
                                    concat_stack.pop());
                                while (concat_stack.size() > 0){
                                   nfa1 =  concat(nfa1, concat_stack.pop());
                                }
                            }
                            else{
                                nfa1 = operands.pop();
                            }
                            operands.push(union(nfa1, nfa2));
                        }
                    }
                }
                else if (c == '*'){
                    operands.push(kleene(operands.pop()));
                    ccflag = true;
                }
                else if (c == '('){ // if any other operator: push
                    operators.push(c);
                    para_count++;
                }
                else if (c == '|'){
                    operators.push(c);
                    ccflag = false;
                }
            }
        }
        while (operators.size() > 0){
            if (operands.empty()){
                System.out.println("Error: imbalanace in operands and "
                + "operators");
                System.exit(1);
            }
            op = operators.pop();
            if (op == '.'){
                nfa2 = operands.pop();
                nfa1 = operands.pop();
                operands.push(concat(nfa1, nfa2));
            }
            else if (op == '|'){
                nfa2 = operands.pop();
                if( !operators.empty() && operators.peek() == '.'){
                    concat_stack.push(operands.pop());
                    while (!operators.empty() && operators.peek() == '.'){
                        concat_stack.push(operands.pop());
                        operators.pop();
                    }
                    nfa1 = concat(concat_stack.pop(),
                        concat_stack.pop());
                    while (concat_stack.size() > 0){
                       nfa1 =  concat(nfa1, concat_stack.pop());
                    }
                }
                else{
                    nfa1 = operands.pop();
                }
                operands.push(union(nfa1, nfa2));
            }
        }
        return operands.pop();
    }

    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        String line;
        System.out.println("\nEnter a regular expression with the " +
            "alphabet ['a','z'] & E for empty "+"\n* for Kleene Star" + 
            "\nelements with nothing between them indicates " +
            "concatenation "+ "\n| for Union \n\":q\" to quit");
        while(sc.hasNextLine()){
            System.out.println("Enter a regular expression with the " +
                "alphabet ['a','z'] & E for empty "+"\n* for Kleene Star" + 
                "\nelements with nothing between them indicates " +
                "concatenation "+ "\n| for Union \n\":q\" to quit");
            line = sc.nextLine();
            if (line.equals(":q") || line.equals("QUIT"))
                break;
            NFA nfa_of_input = compile(line);
            System.out.println("\nNFA:");
            nfa_of_input.display();
        }
    }
}

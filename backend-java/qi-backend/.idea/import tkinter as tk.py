import tkinter as tk
import random
import winsound

data = [

# ---------- DIGITAL ELECTRONICS ----------

("Binary of decimal 25?", ["11001","10101","11100","10011"], "11001"),
("Decimal value of binary 11010?", ["24","26","28","30"], "26"),
("2's complement of 0101?", ["1010","1011","1101","1001"], "1011"),
("1's complement of 1010?", ["0101","1010","1111","0000"], "0101"),
("Binary 1111 equals hexadecimal?", ["A","B","E","F"], "F"),
("Hexadecimal A equals decimal?", ["8","10","12","16"], "10"),
("Binary 101 equals decimal?", ["3","4","5","6"], "5"),
("How many bits are in a nibble?", ["2","4","8","16"], "4"),

("NOT(AND) represents which gate?", ["NOR","NAND","XOR","XNOR"], "NAND"),
("NOT(OR) represents which gate?", ["NAND","NOR","XOR","AND"], "NOR"),
("XOR output is HIGH when?", ["Inputs same","Inputs different","Both LOW","Both HIGH"], "Inputs different"),
("XNOR output is HIGH when?", ["Inputs different","Inputs same","Only A HIGH","Only B HIGH"], "Inputs same"),
("Which gate is a universal gate?", ["AND","OR","NAND","XOR"], "NAND"),
("Another universal gate is?", ["NOR","AND","XOR","XNOR"], "NOR"),
("Output of 1 AND 0?", ["0","1","10","Undefined"], "0"),
("Output of 1 OR 0?", ["0","1","10","Undefined"], "1"),
("Output of NOT 1?", ["0","1","10","None"], "0"),

("How many select lines for 8:1 MUX?", ["2","3","4","8"], "3"),
("How many inputs does 4:1 MUX have?", ["2","4","6","8"], "4"),
("How many outputs does a MUX have?", ["1","2","4","8"], "1"),
("A DEMUX performs?", ["One-to-many","Many-to-one","Addition","Subtraction"], "One-to-many"),
("Encoder performs?", ["Binary to decimal","Decimal to binary","Analog conversion","Counting"], "Decimal to binary"),
("Decoder performs?", ["Binary to output lines","Output to binary","Addition","Storage"], "Binary to output lines"),

("A half adder has?", ["2 inputs, 1 output","2 inputs, 2 outputs","3 inputs, 2 outputs","1 input, 2 outputs"], "2 inputs, 2 outputs"),
("A full adder has how many inputs?", ["1","2","3","4"], "3"),
("A full adder has how many outputs?", ["1","2","3","4"], "2"),
("Which circuit is used to add binary numbers?", ["Adder","Decoder","Encoder","MUX"], "Adder"),
("Which output of a half adder represents carry?", ["AND","OR","XOR","NOT"], "AND"),

("Which flip-flop has J and K inputs?", ["D","T","JK","SR"], "JK"),
("Which flip-flop has only one data input?", ["D","JK","SR","T"], "D"),
("Which flip-flop is commonly used for toggling?", ["D","T","SR","JK"], "T"),
("How many stable states does a flip-flop have?", ["1","2","3","4"], "2"),
("Which flip-flop is called a delay flip-flop?", ["D","JK","SR","T"], "D"),

("Which counter changes flip-flops simultaneously?", ["Ripple","Asynchronous","Synchronous","Ring"], "Synchronous"),
("Ripple counter is also called?", ["Synchronous","Asynchronous","Parallel","Johnson"], "Asynchronous"),
("Main disadvantage of ripple counter?", ["High cost","Propagation delay","Low speed only","High voltage"], "Propagation delay"),
("A counter is mainly used for?", ["Counting pulses","Amplifying","Rectifying","Filtering"], "Counting"),

("Which memory is volatile?", ["ROM","RAM","EEPROM","Flash"], "RAM"),
("Which memory is non-volatile?", ["RAM","ROM","Cache","Register"], "ROM"),
("Which memory is fastest?", ["Hard disk","RAM","Cache","DVD"], "Cache"),
("Which memory is used for temporary data?", ["ROM","RAM","EEPROM","Flash"], "RAM"),

("ADC stands for?", ["Analog Digital Converter","Analog-to-Digital Converter","Automatic Digital Control","Analog Data Circuit"], "Analog-to-Digital Converter"),
("DAC stands for?", ["Digital-to-Analog Converter","Data Analog Circuit","Digital Automatic Control","Direct Analog Converter"], "Digital-to-Analog Converter"),
("ADC converts?", ["Digital to analog","Analog to digital","AC to DC","DC to AC"], "Analog to digital"),
("DAC converts?", ["Digital to analog","Analog to digital","AC to DC","DC to AC"], "Digital to analog"),

("Which logic family has low power consumption?", ["TTL","CMOS","RTL","DTL"], "CMOS"),
("TTL mainly uses which device?", ["MOSFET","BJT","JFET","IGBT"], "BJT"),
("CMOS mainly uses?", ["MOSFET","BJT","SCR","Diode"], "MOSFET"),

("How many states does binary logic have?", ["1","2","3","4"], "2"),
("Binary number system has base?", ["2","8","10","16"], "2"),
("Octal number system has base?", ["2","8","10","16"], "8"),
("Hexadecimal number system has base?", ["2","8","10","16"], "16"),

("How many bits are required to represent 16 different values?", ["2","3","4","8"], "4"),
("How many combinations are possible with 3 bits?", ["3","6","8","9"], "8"),
("How many combinations are possible with 4 bits?", ["8","12","16","32"], "16"),

# ---------- GENERAL KNOWLEDGE ----------

("Largest planet?", ["Earth","Mars","Jupiter","Saturn"], "Jupiter"),
("Largest ocean?", ["Atlantic","Indian","Pacific","Arctic"], "Pacific"),
("Capital of India?", ["Mumbai","Delhi","Kolkata","Chennai"], "Delhi"),
("Fastest land animal?", ["Lion","Horse","Cheetah","Tiger"], "Cheetah"),
("Most abundant gas in atmosphere?", ["Oxygen","Nitrogen","CO2","Hydrogen"], "Nitrogen"),
("SI unit of electric charge?", ["Volt","Ampere","Coulomb","Ohm"], "Coulomb"),
("Speed of light is approximately?", ["3×10⁶","3×10⁸","3×10¹⁰","3×10⁴"], "3×10⁸"),
("Hardest natural substance?", ["Iron","Gold","Diamond","Copper"], "Diamond"),
("Which planet is called the Red Planet?", ["Venus","Mars","Jupiter","Mercury"], "Mars"),
("National animal of India?", ["Lion","Tiger","Elephant","Peacock"], "Tiger")
]
score = 0
q = 0
questions = random.sample(data, 15)

def show():
    question.config(text=f"Question {q+1}/15\n\n{questions[q][0]}")
    
    for i in range(4):
        buttons[i].config(
            text=questions[q][1][i],
            state="normal",
            command=lambda x=questions[q][1][i]: check(x)
        )

def check(answer):
    global score, q

    for b in buttons:
        b.config(state="disabled")

    if answer == questions[q][2]:
        score += 10
        result.config(text="✅ Correct!", fg="green")
        winsound.Beep(1000, 150)
    else:
        result.config(
            text="❌ Wrong!\nCorrect answer: " + questions[q][2],
            fg="red"
        )
        winsound.Beep(300, 300)

    q += 1

    if q < 15:
        root.after(1500, show)
    else:
        root.after(1500, finish)

def finish():
    result.config(
        text=f"🏆 FINAL SCORE: {score}/150",
        fg="blue"
    )
    restart.config(state="normal")

def restart_game():
    global score, q, questions

    score = 0
    q = 0
    questions = random.sample(data, 15)

    result.config(text="")
    restart.config(state="disabled")
    show()

root = tk.Tk()
root.title("⚡ Engineering Quiz Challenge")
root.geometry("650x600")
root.configure(bg="#dff6ff")

tk.Label(
    root,
    text="⚡ ENGINEERING QUIZ",
    font=("Arial", 28, "bold"),
    bg="#dff6ff"
).pack(pady=25)

question = tk.Label(
    root,
    font=("Arial", 19, "bold"),
    bg="#dff6ff",
    wraplength=600
)
question.pack(pady=20)

buttons = []

for i in range(4):
    b = tk.Button(
        root,
        width=30,
        font=("Arial", 14, "bold"),
        bg="white"
    )
    b.pack(pady=5)
    buttons.append(b)

result = tk.Label(
    root,
    font=("Arial", 17, "bold"),
    bg="#dff6ff"
)
result.pack(pady=20)

restart = tk.Button(
    root,
    text="🔄 PLAY AGAIN",
    command=restart_game,
    font=("Arial", 14, "bold"),
    state="disabled"
)
restart.pack()

show()
root.mainloop()
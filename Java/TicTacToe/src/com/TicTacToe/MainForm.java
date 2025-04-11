/*
 *   TicTacToe
 *
 *   A game you can be an Attacker or Defender, as a user you may put an O
 *   chess while the program might response from a X chess. Which of the
 *   roles also gives you a chance to simulate within various cases in Debug
 *   mode. The code enumerates a course of options, the modes are encoded in
 *   the 2-bit field from a 32-bit type Board, and the one exceeding 2-bit is
 *   treated as a control code to NewGame. The Startup code intends to just
 *   reset the game without switching into other encoded mode. The Conjugate
 *   code switches in between Attacker or Defender while the Configure code
 *   may on or off the Debug mode when you press the key D or Escape. The
 *   Conjugate code combining the Configure code reproduces 4 scene, which
 *   of those can further jump in Bonus scene or Clumsy scene, where you
 *   press the key W or L. Whenever you press the key Escape, you will
 *   ultimately get in the original scene you held.
 *
 *   Copyright (C) 2025  Edmond Chow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *   If you have any inquiry, feel free to contact <edmond-chow@outlook.com>.
 */
package com.TicTacToe;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
public class MainForm extends JDialog {
    private enum Mode {
        Attacker(0),
        Defender(1),
        DebugAttacker(2),
        DebugDefender(3),
        StartupMode(4),
        ConjugateMode(5),
        ConfigureMode(6),
        BonusScene(7),
        ClumsyScene(8);
        private final int value;
        Mode(int value) {
            this.value = value;
        }
        public int toInt() {
            return this.value;
        }
        public static Mode from(int value) {
            for (Mode v : Mode.values()) {
                if (v.value == value) {
                    return v;
                }
            }
            return Mode.StartupMode;
        }
    }
    private enum Turn {
        Unspecified(0),
        User(1),
        Response(2),
        Terminated(3);
        private final int value;
        Turn(int value) {
            this.value = value;
        }
        public int toInt() {
            return this.value;
        }
        public static Turn from(int value) {
            for (Turn v : Turn.values()) {
                if (v.value == value) {
                    return v;
                }
            }
            return Turn.Unspecified;
        }
    }
    private enum Result {
        None(0),
        Won(1),
        Lost(2),
        Tied(3);
        private final int value;
        Result(int value) {
            this.value = value;
        }
        public int toInt() {
            return this.value;
        }
        public static Result from(int value) {
            for (Result v : Result.values()) {
                if (v.value == value) {
                    return v;
                }
            }
            return Result.None;
        }
    }
    private enum Chess {
        None(0),
        X(1),
        O(2),
        Preferred(3);
        private final int value;
        Chess(int value) {
            this.value = value;
        }
        public int toInt() {
            return this.value;
        }
        public static Chess from(int value) {
            for (Chess v : Chess.values()) {
                if (v.value == value) {
                    return v;
                }
            }
            return Chess.None;
        }
    }
    private enum Orientation {
        Horizontal(0),
        Upward(1),
        Vertical(2),
        Downward(3);
        private final int value;
        Orientation(int value) {
            this.value = value;
        }
        public int toInt() {
            return this.value;
        }
        public static Orientation from(int value) {
            for (Orientation v : Orientation.values()) {
                if (v.value == value) {
                    return v;
                }
            }
            return Orientation.Horizontal;
        }
    }
    private static class Board {
        private static final int MaskFst3 = 0x3F00;
        private static final int Box = 0b11;
        private static final int Conjg = 0b1;
        private static final int Confg = 0b10;
        private static final int P1 = 0b1;
        private static final int P2 = 0b10;
        private static final int P4 = 0b100;
        private static final int P8 = 0b1000;
        private static final int IRound = 0;
        private static final int IMode = 10;
        private static final int ITurn = 11;
        private static final int IResult = 12;
        private static final int IState = 13;
        private static final int[] Offset;
        private static final int[] Mask;
        static {
            Offset = new int[] { 20, 12, 10, 8, 14, 24, 6, 0, 2, 4, 30, 28, 26, 16 };
            Mask = new int[Offset.length];
            Mask[IRound] = 0xF00000;
            Mask[IState] = 0xF0000;
            for (int i = 1; i < Offset.length - 1; ++i) {
                Mask[i] = Box << Offset[i];
            }
        }
        private int Data;
        public Chess get(int i) {
            if (i < 1 || i > 9) { throw new IndexOutOfBoundsException(); }
            return Chess.from((Data & Mask[i]) >>> Offset[i]);
        }
        public void set(int i, Chess value) {
            if (i < 1 || i > 9) { throw new IndexOutOfBoundsException(); }
            Data &= ~Mask[i];
            Data |= (value.toInt() << Offset[i]) & Mask[i];
        }
        public Mode getMode() {
            return Mode.from((Data & Mask[IMode]) >>> Offset[IMode]);
        }
        public void setMode(Mode value) {
            Data &= ~Mask[IMode];
            Data |= (value.toInt() << Offset[IMode]) & Mask[IMode];
        }
        public Mode getConjugateMode() {
            return Mode.from((getMode().toInt() & Confg) | (~getMode().toInt() & Conjg));
        }
        public Mode getConfigureMode() {
            return Mode.from((getMode().toInt() & Conjg) | (~getMode().toInt() & Confg));
        }
        public boolean onDefenderSide() {
            return (getMode().toInt() & Conjg) == Conjg;
        }
        public boolean inDebugMode() {
            return (getMode().toInt() & Confg) == Confg;
        }
        public Turn getTurn() {
            return Turn.from((Data & Mask[ITurn]) >>> Offset[ITurn]);
        }
        public void setTurn(Turn value) {
            Data &= ~Mask[ITurn];
            Data |= (value.toInt() << Offset[ITurn]) & Mask[ITurn];
        }
        public Result getResult() {
            return Result.from((Data & Mask[IResult]) >>> Offset[IResult]);
        }
        public void setResult(Result value) {
            Data &= ~Mask[IResult];
            Data |= (value.toInt() << Offset[IResult]) & Mask[IResult];
        }
        public int getRound() {
            return (Data & Mask[IRound]) >>> Offset[IRound];
        }
        public void setRound(int value) {
            Data &= ~Mask[IRound];
            Data |= (value << Offset[IRound]) & Mask[IRound];
        }
        public int getState() {
            return (Data & Mask[IState]) >>> Offset[IState];
        }
        private void setState(int value) {
            Data &= ~Mask[IState];
            Data |= (value << Offset[IState]) & Mask[IState];
        }
        public int getMoves() {
            return getState() & 0b111;
        }
        public Orientation getOrient() {
            return Orientation.from(getState() & Box);
        }
        public boolean getParse1() {
            return (getState() & P1) == P1;
        }
        public void setParse1(boolean value) {
            boolean Origin = (getState() & P1) == P1;
            if (Origin == value) { return; }
            if (value) {
                rotate(1);
                setState(getState() | P1);
            } else {
                rotate(-1);
                setState(getState() & ~P1);
            }
        }
        public boolean getParse2() {
            return (getState() & P2) == P2;
        }
        public void setParse2(boolean value) {
            boolean Origin = (getState() & P2) == P2;
            if (Origin == value) { return; }
            if (value) {
                rotate(2);
                setState(getState() | P2);
            } else {
                rotate(-2);
                setState(getState() & ~P2);
            }
        }
        public boolean getParse4() {
            return (getState() & P4) == P4;
        }
        public void setParse4(boolean value) {
            boolean Origin = (getState() & P4) == P4;
            if (Origin == value) { return; }
            if (value) {
                rotate(4);
                setState(getState() | P4);
            } else {
                rotate(-4);
                setState(getState() & ~P4);
            }
        }
        public boolean getParse8() {
            return (getState() & P8) == P8;
        }
        public void setParse8(boolean value) {
            boolean Origin = (getState() & P8) == P8;
            if (Origin == value) { return; }
            reflect(getOrient());
            if (value) { setState(getState() | P8); }
            else { setState(getState() & ~P8); }
        }
        public int getCase() {
            int Rst = (Data & MaskFst3) >>> 6;
            for (int i = 4; i <= 9; ++i) {
                Rst <<= i == 7 ? 4 : 2;
                Rst |= (Data & Mask[i]) >>> Offset[i];
            }
            return Rst;
        }
        public void setCase(int value) {
            Data &= ~MaskFst3;
            Data |= (value >>> 8) & MaskFst3;
            int Rest = value & 0xFFFF;
            for (int i = 9; i >= 4; --i) {
                Data &= ~Mask[i];
                Data |= (Rest & Box) << Offset[i];
                Rest >>>= i == 7 ? 4 : 2;
            }
        }
        public Board getSanitizer() {
            Board Rst = clone();
            for (int i = 1; i <= 9; ++i) {
                if (Rst.get(i) == Chess.Preferred) { Rst.set(i, Chess.None); }
            }
            return Rst;
        }
        public Board(Mode mode) {
            Data = 0;
            if ((mode.toInt() & Conjg) == 0b0) { setTurn(Turn.User); }
            else { setTurn(Turn.Response); }
            setMode(mode);
        }
        public Board(int match) {
            Data = 0;
            setCase(match);
        }
        public ArrayList<Integer> locateChesses(Chess chess) {
            ArrayList<Integer> Rst = new ArrayList<Integer>(9);
            for (int i = 1; i <= 9; ++i) {
                if (get(i) == chess) { Rst.add(i); }
            }
            return Rst;
        }
        public Board[] parses(int state)
        {
            boolean C1 = (state & P1) == P1;
            boolean C2 = (state & P2) == P2;
            boolean C4 = (state & P4) == P4;
            boolean C8 = (state & P8) == P8;
            boolean V1 = getParse1();
            boolean V2 = getParse2();
            boolean V4 = getParse4();
            boolean V8 = getParse8();
            int Sz = 1;
            if (C1) { Sz *= 2; }
            if (C2) { Sz *= 2; }
            if (C4) { Sz *= 2; }
            if (C8) { Sz *= 2; }
            Board[] Rst = new Board[Sz];
            for (int i = 0; i < Rst.length; ++i) {
                Rst[i] = clone();
            }
            int Dx = 1;
            if (C1) {
                int Dy = Dx;
                Dx *= 2;
                for (int i = Dy; i < Rst.length; ++i) {
                    if (i % Dx == 0) { i += Dy; }
                    Rst[i].setParse1(!V1);
                }
            }
            if (C2) {
                int Dy = Dx;
                Dx *= 2;
                for (int i = Dy; i < Rst.length; ++i) {
                    if (i % Dx == 0) { i += Dy; }
                    Rst[i].setParse2(!V2);
                }
            }
            if (C4) {
                int Dy = Dx;
                Dx *= 2;
                for (int i = Dy; i < Rst.length; ++i) {
                    if (i % Dx == 0) { i += Dy; }
                    Rst[i].setParse4(!V4);
                }
            }
            if (C8) {
                int Dy = Dx;
                Dx *= 2;
                for (int i = Dy; i < Rst.length; ++i) {
                    if (i % Dx == 0) { i += Dy; }
                    Rst[i].setParse8(!V8);
                }
            }
            return Rst;
        }
        @Override
        public String toString() {
            StringBuilder Rst = new StringBuilder();
            Rst.append("[ ");
            for (int i = 1; i <= 9; ++i) {
                if (get(i) == Chess.None) { Rst.append("?"); }
                else if (get(i) == Chess.X) { Rst.append("X"); }
                else if (get(i) == Chess.O) { Rst.append("O"); }
                else if (get(i) == Chess.Preferred) { Rst.append("+"); }
                if (i == 3 || i == 6) { Rst.append(", "); }
            }
            Rst.append(" ]");
            return Rst.toString();
        }
        public Board clone() {
            Board Rst = new Board(Mode.Attacker);
            Rst.Data = Data;
            return Rst;
        }
        public void rotate(int moves) {
            if (moves < 0) { moves = 8 - moves; }
            moves %= 8;
            int Nears = (Data & 0xFFFF) << moves * 2;
            Nears |= (Nears & 0xFFFF0000) >>> 16;
            Data = (Data & 0xFFFF0000) | (Nears & 0xFFFF);
        }
        public void reflect(Orientation Orient) {
            if (Orient == Orientation.Horizontal) {
                int Lines = getCase();
                int First = (Lines & 0xFF0000) >>> 16;
                int Last = (Lines & 0xFF) << 16;
                Lines &= 0xFF00;
                Lines |= First;
                Lines |= Last;
                setCase(Lines);
            } else if (Orient == Orientation.Upward) {
                rotate(-1);
                reflect(Orientation.Horizontal);
                rotate(1);
            } else if (Orient == Orientation.Vertical) {
                rotate(2);
                reflect(Orientation.Horizontal);
                rotate(-2);
            } else if (Orient == Orientation.Downward) {
                rotate(1);
                reflect(Orientation.Horizontal);
                rotate(-1);
            }
        }
        public void clearParse() {
            rotate(-getMoves());
            if (getParse8()) { reflect(Orientation.Horizontal); }
            setState(0);
        }
        public void reset() {
            Data = 0;
        }
    }
    private static class Pack {
        private final int Data;
        private final Board[] Parses;
        public Board[] getBoards() {
            Board[] Rst = Parses.clone();
            for (int i = 0; i < Rst.length; ++i) {
                Rst[i] = Rst[i].clone();
            }
            return Rst;
        }
        public int getSource() {
            return Data;
        }
        public Pack(int Source) {
            Data = Source;
            Parses = new Board(Source).parses(Source >>> 24);
        }
    }
    private static final Pack MaskA = new Pack(0b1111_00111111_00111111_00111111);
    private static final Pack WonC = new Pack(0b0011_00001000_00001000_00001000);
    private static final Pack LostC = new Pack(0b0011_00000100_00000100_00000100);
    private static final Pack MaskC = new Pack(0b0011_00001100_00001100_00001100);
    private static final Pack WonS = new Pack(0b0110_00100000_00100000_00100000);
    private static final Pack LostS = new Pack(0b0110_00010000_00010000_00010000);
    private static final Pack MaskS = new Pack(0b0110_00110000_00110000_00110000);
    private static final Pack WonCN = new Pack(0b0111_00001000_00001000_00001100);
    private static final Pack LostCN = new Pack(0b0111_00000100_00000100_00001100);
    private static final Pack MaskCN = new Pack(0b0111_00001100_00001100_00001100);
    private static final Pack WonCM = new Pack(0b0011_00001000_00001100_00001000);
    private static final Pack LostCM = new Pack(0b0011_00000100_00001100_00000100);
    private static final Pack MaskCM = new Pack(0b0011_00001100_00001100_00001100);
    private static final Pack WonSN = new Pack(0b1110_00100000_00100000_00110000);
    private static final Pack LostSN = new Pack(0b1110_00010000_00010000_00110000);
    private static final Pack MaskSN = new Pack(0b1110_00110000_00110000_00110000);
    private static final Pack WonSM = new Pack(0b0110_00100000_00110000_00100000);
    private static final Pack LostSM = new Pack(0b0110_00010000_00110000_00010000);
    private static final Pack MaskSM = new Pack(0b0110_00110000_00110000_00110000);
    private static final Pack[] Cases = new Pack[] {
        new Pack(0b0000_00110011_00001100_00110011),
        new Pack(0b0111_00001000_00001100_00000000),
        new Pack(0b0110_00001100_00110111_00111011),
        new Pack(0b0110_00011111_00111000_00110000),
        new Pack(0b0110_00011100_00111111_00001110),
        new Pack(0b1110_00011011_00110011_00111111),
        new Pack(0b1110_00011110_00001111_00001100),
        new Pack(0b1110_00111101_00100011_00111111),
        new Pack(0b0010_00100011_00000100_00110010),
        new Pack(0b0110_00001000_00100111_00001111),
        new Pack(0b0110_00011100_00111011_00001110),
        new Pack(0b1110_00110010_00100100_00111100),
        new Pack(0b1110_00000010_00100101_00000011),
        new Pack(0b1110_00001101_00110110_00101111),
        new Pack(0b1110_00011110_00101111_00011100),
    };
    private final Container[] Co;
    private Mode LstMo;
    private String getShownText() {
        String Rst = Bo.inDebugMode() ? "< Debug > " : "";
        if (LstMo != Mode.StartupMode) { Rst = Bo.onDefenderSide() ? "< Clumsy > " : "< Bonus > "; }
        Rst += "TicTacToe";
        Rst += Bo.onDefenderSide() ? " Defender" : " Attacker";
        if (Bo.getResult() == Result.Won) { Rst += " [ Win ]"; }
        else if (Bo.getResult() == Result.Lost) { Rst += " [ Lost ]"; }
        else if (Bo.getResult() == Result.Tied) { Rst += " [ Tied ]"; }
        return Rst;
    }
    private Board Bo;
    private Mode getMo() {
        return Bo.getMode();
    }
    private void setMo(Mode value) {
        if (Bo.getMode() == value) { return; }
        Bo = new Board(value);
        setTitle(getShownText());
    }
    private Turn getTu() {
        return Bo.getTurn();
    }
    private void setTu(Turn value) {
        if (value == Turn.Unspecified) {
            Bo = new Board(Bo.getMode());
        } else if (value == Turn.Terminated || Bo.getTurn() == Turn.Terminated) {
            Bo.setTurn(Turn.Terminated);
            Bo.setRound(9);
        } else if (Bo.getTurn() != value && Bo.getRound() < 9) {
            Bo.setTurn(value);
            Bo.setRound(Bo.getRound() + 1);
        }
        setTitle(getShownText());
    }
    private Result getRe() {
        return Bo.getResult();
    }
    private void setRe(Result value) {
        if (Bo.getResult() == value) { return; }
        if (value == Result.None) {
            Bo = new Board(Bo.getMode());
        } else {
            Bo.setTurn(Turn.Terminated);
            Bo.setRound(9);
        }
        Bo.setResult(value);
        setTitle(getShownText());
    }
    private static final Color GhostWhite = new Color(0xF8F8FF);
    private static final Color AliceBlue = new Color(0xF0F8FF);
    private static final Color DodgerBlue = new Color(0x1E90FF);
    private static final Color LightSeaGreen = new Color(0x20B2AA);
    private static final Color LightSteelBlue = new Color(0xB0C4DE);
    private static final Color Green = new Color(0x008000);
    private static final Color Red = new Color(0xFF0000);
    private JButton Button1;
    private JButton Button2;
    private JButton Button3;
    private JButton Button4;
    private JButton Button5;
    private JButton Button6;
    private JButton Button7;
    private JButton Button8;
    private JButton Button9;
    private JButton ButtonSwitch;
    private JButton ButtonReset;
    private JPanel Panel;
    private void initializeComponent() {
        Button1 = new JButton();
        Button2 = new JButton();
        Button3 = new JButton();
        Button4 = new JButton();
        Button5 = new JButton();
        Button6 = new JButton();
        Button7 = new JButton();
        Button8 = new JButton();
        Button9 = new JButton();
        ButtonSwitch = new JButton();
        ButtonReset = new JButton();
        Panel = new JPanel();
        Button1.setName("Button1");
        Button2.setName("Button2");
        Button3.setName("Button3");
        Button4.setName("Button4");
        Button5.setName("Button5");
        Button6.setName("Button6");
        Button7.setName("Button7");
        Button8.setName("Button8");
        Button9.setName("Button9");
        ButtonSwitch.setName("ButtonSwitch");
        ButtonReset.setName("ButtonReset");
        Panel.setName("Panel");
        Button1.setBounds(12, 12, 80, 80);
        Button2.setBounds(98, 12, 80, 80);
        Button3.setBounds(184, 12, 80, 80);
        Button4.setBounds(12, 98, 80, 80);
        Button5.setBounds(98, 98, 80, 80);
        Button6.setBounds(184, 98, 80, 80);
        Button7.setBounds(12, 184, 80, 80);
        Button8.setBounds(98, 184, 80, 80);
        Button9.setBounds(184, 184, 80, 80);
        ButtonSwitch.setBounds(12, 270, 123, 35);
        ButtonReset.setBounds(141, 270, 123, 35);
        Panel.setPreferredSize(new Dimension(276, 317));
        Button1.setBackground(AliceBlue);
        Button2.setBackground(AliceBlue);
        Button3.setBackground(AliceBlue);
        Button4.setBackground(AliceBlue);
        Button5.setBackground(AliceBlue);
        Button6.setBackground(AliceBlue);
        Button7.setBackground(AliceBlue);
        Button8.setBackground(AliceBlue);
        Button9.setBackground(AliceBlue);
        ButtonSwitch.setBackground(AliceBlue);
        ButtonReset.setBackground(AliceBlue);
        Panel.setBackground(LightSteelBlue);
        Button1.setBorder(new LineBorder(DodgerBlue));
        Button2.setBorder(new LineBorder(DodgerBlue));
        Button3.setBorder(new LineBorder(DodgerBlue));
        Button4.setBorder(new LineBorder(DodgerBlue));
        Button5.setBorder(new LineBorder(DodgerBlue));
        Button6.setBorder(new LineBorder(DodgerBlue));
        Button7.setBorder(new LineBorder(DodgerBlue));
        Button8.setBorder(new LineBorder(DodgerBlue));
        Button9.setBorder(new LineBorder(DodgerBlue));
        ButtonSwitch.setBorder(new LineBorder(DodgerBlue));
        ButtonReset.setBorder(new LineBorder(DodgerBlue));
        Button1.setFont(Button1.getFont().deriveFont(20f));
        Button2.setFont(Button1.getFont().deriveFont(20f));
        Button3.setFont(Button1.getFont().deriveFont(20f));
        Button4.setFont(Button1.getFont().deriveFont(20f));
        Button5.setFont(Button1.getFont().deriveFont(20f));
        Button6.setFont(Button1.getFont().deriveFont(20f));
        Button7.setFont(Button1.getFont().deriveFont(20f));
        Button8.setFont(Button1.getFont().deriveFont(20f));
        Button9.setFont(Button1.getFont().deriveFont(20f));
        ButtonSwitch.setText("Switch");
        ButtonReset.setText("Reset");
        ButtonReset.setEnabled(false);
        getContentPane().add(Button1);
        getContentPane().add(Button2);
        getContentPane().add(Button3);
        getContentPane().add(Button4);
        getContentPane().add(Button5);
        getContentPane().add(Button6);
        getContentPane().add(Button7);
        getContentPane().add(Button8);
        getContentPane().add(Button9);
        getContentPane().add(ButtonSwitch);
        getContentPane().add(ButtonReset);
        getContentPane().add(Panel);
        pack();
        setName("MainForm");
        setResizable(false);
        setTitle("TicTacToe");
        setLocationRelativeTo(null);
        setIconImage(new ImageIcon(getClass().getResource( "/TicTacToe.png" )).getImage());
        addWindowListener(new FormListener());
        Button1.addActionListener(new ChessListener());
        Button2.addActionListener(new ChessListener());
        Button3.addActionListener(new ChessListener());
        Button4.addActionListener(new ChessListener());
        Button5.addActionListener(new ChessListener());
        Button6.addActionListener(new ChessListener());
        Button7.addActionListener(new ChessListener());
        Button8.addActionListener(new ChessListener());
        Button9.addActionListener(new ChessListener());
        ButtonSwitch.addActionListener(new SwitchListener());
        ButtonReset.addActionListener(new ResetListener());
        Button1.addFocusListener(new ChessListener());
        Button2.addFocusListener(new ChessListener());
        Button3.addFocusListener(new ChessListener());
        Button4.addFocusListener(new ChessListener());
        Button5.addFocusListener(new ChessListener());
        Button6.addFocusListener(new ChessListener());
        Button7.addFocusListener(new ChessListener());
        Button8.addFocusListener(new ChessListener());
        Button9.addFocusListener(new ChessListener());
        ButtonSwitch.addFocusListener(new ChessListener());
        ButtonReset.addFocusListener(new ChessListener());
        Button1.addMouseListener(new ChessListener());
        Button2.addMouseListener(new ChessListener());
        Button3.addMouseListener(new ChessListener());
        Button4.addMouseListener(new ChessListener());
        Button5.addMouseListener(new ChessListener());
        Button6.addMouseListener(new ChessListener());
        Button7.addMouseListener(new ChessListener());
        Button8.addMouseListener(new ChessListener());
        Button9.addMouseListener(new ChessListener());
        ButtonSwitch.addMouseListener(new ChessListener());
        ButtonReset.addMouseListener(new ChessListener());
        Button1.addKeyListener(new ChessListener());
        Button2.addKeyListener(new ChessListener());
        Button3.addKeyListener(new ChessListener());
        Button4.addKeyListener(new ChessListener());
        Button5.addKeyListener(new ChessListener());
        Button6.addKeyListener(new ChessListener());
        Button7.addKeyListener(new ChessListener());
        Button8.addKeyListener(new ChessListener());
        Button9.addKeyListener(new ChessListener());
        ButtonSwitch.addKeyListener(new ChessListener());
        ButtonReset.addKeyListener(new ChessListener());
    }
    public MainForm() {
        super((JDialog)null);
        initializeComponent();
        LstMo = Mode.StartupMode;
        Bo = new Board(Mode.Attacker);
        Co = new Container[] { this, Button1, Button2, Button3, Button4, Button5, Button6, Button7, Button8, Button9, ButtonSwitch, ButtonReset };
    }
    public static void run(MainForm form) {
        form.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        form.setVisible(true);
    }
    private void chooseChess(ArrayList<Integer> chosen) {
        putChess(Co[chosen.get(new Random().nextInt(chosen.size()))]);
    }
    private boolean processResponse(Board[] match, Board[] mask) {
        for (int i = 0; i < match.length; ++i) {
            if ((Bo.getCase() & mask[i].getCase()) == match[i].getSanitizer().getCase()) {
                chooseChess(match[i].locateChesses(Chess.Preferred));
                return true;
            }
        }
        return false;
    }
    private void checkResponse() {
        Board[] PMask = MaskA.getBoards();
        for (Pack match : Cases) {
            if (processResponse(match.getBoards(), PMask)) { return; }
        }
        Board[] PWonCN = WonCN.getBoards();
        Board[] PLostCN = LostCN.getBoards();
        Board[] PMaskCN = MaskCN.getBoards();
        Board[] PWonCM = WonCM.getBoards();
        Board[] PLostCM = LostCM.getBoards();
        Board[] PMaskCM = MaskCM.getBoards();
        Board[] PWonSN = WonSN.getBoards();
        Board[] PLostSN = LostSN.getBoards();
        Board[] PMaskSN = MaskSN.getBoards();
        Board[] PWonSM = WonSM.getBoards();
        Board[] PLostSM = LostSM.getBoards();
        Board[] PMaskSM = MaskSM.getBoards();
        if (processResponse(PLostCN, PMaskCN)) { return; }
        if (processResponse(PLostCM, PMaskCM)) { return; }
        if (processResponse(PLostSN, PMaskSN)) { return; }
        if (processResponse(PLostSM, PMaskSM)) { return; }
        if (processResponse(PWonCN, PMaskCN)) { return; }
        if (processResponse(PWonCM, PMaskCM)) { return; }
        if (processResponse(PWonSN, PMaskSN)) { return; }
        if (processResponse(PWonSM, PMaskSM)) { return; }
        chooseChess(Bo.locateChesses(Chess.None));
    }
    private boolean processResult(Board[] match, Board[] mask, Result result) {
        for (int i = 0; i < match.length; ++i) {
            if ((Bo.getCase() & mask[i].getCase()) == match[i].getCase()) {
                setRe(result);
                return true;
            }
        }
        return false;
    }
    private void checkResult() {
        if (getRe() != Result.None) { return; }
        Board[] PWonC = WonC.getBoards();
        Board[] PLostC = LostC.getBoards();
        Board[] PMaskC = MaskC.getBoards();
        Board[] PWonS = WonS.getBoards();
        Board[] PLostS = LostS.getBoards();
        Board[] PMaskS = MaskS.getBoards();
        if (processResult(PWonC, PMaskC, Result.Won)) { return; }
        if (processResult(PWonS, PMaskS, Result.Won)) { return; }
        if (processResult(PLostC, PMaskC, Result.Lost)) { return; }
        if (processResult(PLostS, PMaskS, Result.Lost)) { return; }
        if (Bo.getRound() == 9) { setRe(Result.Tied); }
    }
    private void newGame(Mode mode) {
        if (mode == Mode.StartupMode || getMo() == mode) { setTu(Turn.Unspecified); }
        else if (mode == Mode.ConjugateMode) { setMo(Bo.getConjugateMode()); }
        else if (mode == Mode.ConfigureMode) { setMo(Bo.getConfigureMode()); }
        else if (mode == Mode.BonusScene) {
            if (LstMo == Mode.StartupMode) { LstMo = getMo(); }
            if (getMo() == Mode.DebugAttacker) { setTu(Turn.Unspecified); }
            else { setMo(Mode.DebugAttacker); }
        } else if (mode == Mode.ClumsyScene) {
            if (LstMo == Mode.StartupMode) { LstMo = getMo(); }
            if (getMo() == Mode.DebugDefender) { setTu(Turn.Unspecified); }
            else { setMo(Mode.DebugDefender); }
        } else {
            setMo(mode);
        }
        ButtonReset.setEnabled(false);
        for (int i = 1; i <= 9; ++i) {
            ((AbstractButton)Co[i]).setText("");
            Co[i].setForeground(Color.black);
        }
        if (LstMo != Mode.StartupMode) {
            putChess(Button1);
            putChess(Button2);
            putChess(Button3);
            putChess(Button6);
            putChess(Button9);
            putChess(Button8);
            putChess(Button7);
            putChess(Button4);
            putChess(Button5);
        } else if (Bo.onDefenderSide() && !Bo.inDebugMode()) { checkResponse(); }
    }
    private void putChess(Container target) {
        ButtonReset.setEnabled(true);
        int i = Arrays.asList(Co).indexOf(target);
        if (Bo.get(i) == Chess.None && getRe() == Result.None) {
            if (getTu() == Turn.User) {
                Bo.set(i, Chess.O);
                ((AbstractButton)target).setText(" O ");
                target.setForeground(Green);
                setTu(Turn.Response);
                checkResult();
                if (!Bo.inDebugMode() && getRe() == Result.None)
                {
                    checkResponse();
                    checkResult();
                }
            } else if (getTu() == Turn.Response) {
                Bo.set(i, Chess.X);
                ((AbstractButton)target).setText(" X ");
                target.setForeground(Red);
                setTu(Turn.User);
                checkResult();
            }
        }
    }
    private void actionSwitchClick(Object sender, ActionEvent e) {
        if (LstMo != Mode.StartupMode) {
            newGame(Bo.onDefenderSide() ? Mode.BonusScene : Mode.ClumsyScene);
        } else {
            newGame(Mode.ConjugateMode);
        }
    }
    private void actionResetClick(Object sender, ActionEvent e) {
        if (LstMo != Mode.StartupMode) {
            Mode Mo = LstMo;
            LstMo = Mode.StartupMode;
            newGame(Mo);
        } else {
            newGame(Mode.StartupMode);
        }
    }
    private void actionFormLoad(Object sender, WindowEvent e) {
        newGame(Mode.StartupMode);
    }
    private void actionChessKeyDown(Object sender, KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_W) {
            newGame(Mode.BonusScene);
        } else if (e.getKeyCode() == KeyEvent.VK_L) {
            newGame(Mode.ClumsyScene);
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE && LstMo != Mode.StartupMode) {
            Mode Mo = LstMo;
            LstMo = Mode.StartupMode;
            newGame(Mo);
        } else if (e.getKeyCode() == KeyEvent.VK_D && !Bo.inDebugMode()) {
            newGame(Bo.getConfigureMode());
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE && Bo.inDebugMode()) {
            newGame(Bo.getConfigureMode());
        }
    }
    private void actionChessClick(Object sender, ActionEvent e) {
        putChess((Container)sender);
    }
    private class FormListener implements WindowListener {
        @Override
        public void windowOpened(WindowEvent e) {
            actionFormLoad(e.getSource(), e);
        }
        @Override
        public void windowClosing(WindowEvent e) {
        }
        @Override
        public void windowClosed(WindowEvent e) {
        }
        @Override
        public void windowIconified(WindowEvent e) {
        }
        @Override
        public void windowDeiconified(WindowEvent e) {
        }
        @Override
        public void windowActivated(WindowEvent e) {
        }
        @Override
        public void windowDeactivated(WindowEvent e) {
        }
    }
    private class ChessListener implements ActionListener, KeyListener, FocusListener, MouseListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            actionChessClick(e.getSource(), e);
        }
        @Override
        public void keyPressed(KeyEvent e) {
            actionChessKeyDown(e.getSource(), e);
        }
        @Override
        public void keyReleased(KeyEvent e) {
        }
        @Override
        public void keyTyped(KeyEvent e) {
        }
        @Override
        public void focusGained(FocusEvent e) {
            ((JComponent)e.getSource()).setBorder(new LineBorder(LightSeaGreen));
        }
        @Override
        public void focusLost(FocusEvent e) {
            ((JComponent)e.getSource()).setBorder(new LineBorder(DodgerBlue));
        }
        @Override
        public void mouseClicked(MouseEvent e) {
        }
        @Override
        public void mousePressed(MouseEvent e) {
        }
        @Override
        public void mouseReleased(MouseEvent e) {
        }
        @Override
        public void mouseEntered(MouseEvent e) {
            ((JComponent)e.getSource()).setBackground(GhostWhite);
        }
        @Override
        public void mouseExited(MouseEvent e) {
            ((JComponent)e.getSource()).setBackground(AliceBlue);
        }
    }
    private class SwitchListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            actionSwitchClick(e.getSource(), e);
        }
    }
    private class ResetListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            actionResetClick(e.getSource(), e);
        }
    }
}

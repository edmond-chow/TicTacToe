/*
 *   TicTacToe
 *
 *   A game you can be an Attacker or Defender, as a User you may put an O
 *   chess while as a Response the program might put an X chess. Which of the
 *   roles also gives you a chance to simulate within various cases in Debug
 *   mode. The code enumerates a course of options, the Modes are encoded in
 *   the 2-bit from a 32-bit Board, and the one exceeding 2-bit is treated as
 *   a control code to NewGame. The Startup code intends to just reset the
 *   game without switching into other encoded Mode. The Conjugate Side
 *   switches in between Attacker or Defender while the Conjugate Form may on
 *   or off the Debug mode when you press the key D or Escape. The Conjugate
 *   Side combining the Conjugate Form reproduces 4 scene, which of those can
 *   further jump in Bonus Scene or Clumsy Scene, where you press the key W
 *   or L. Whenever you press the key Escape, you will ultimately get in the
 *   original scene you held.
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
public class MainWindow extends JDialog {
    private static final int Field = 0xF3F3F3F;
    private static final int Parse = 0xF000000;
    private static final int Match = 0x3F3F3F;
    private enum Mode {
        Attacker(0),
        Defender(1),
        DebugAttacker(2),
        DebugDefender(3),
        StartupMode(4),
        SwitchSide(5),
        SwitchForm(6),
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
        Empty(0),
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
            return Result.Empty;
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
        private static final int First3 = 0x3F00;
        private static final int Circle = 0xFFFF;
        private static final int Center = 0xFF00;
        private static final int Box = 0b11;
        private static final int Side = 0b1;
        private static final int Form = 0b10;
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
        public Mode getConjugateSide() {
            return Mode.from((getMode().toInt() & Form) | (~getMode().toInt() & Side));
        }
        public Mode getConjugateForm() {
            return Mode.from((getMode().toInt() & Side) | (~getMode().toInt() & Form));
        }
        public boolean onDefenderSide() {
            return (getMode().toInt() & Side) == Side;
        }
        public boolean inDebugForm() {
            return (getMode().toInt() & Form) == Form;
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
            int Rst = (Data & First3) >>> 6;
            for (int i = 4; i <= 9; ++i) {
                Rst <<= i == 7 ? 4 : 2;
                Rst |= (Data & Mask[i]) >>> Offset[i];
            }
            return Rst;
        }
        public void setCase(int value) {
            Data &= ~First3;
            Data |= (value >>> 8) & First3;
            int Rest = value;
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
            if ((mode.toInt() & Side) == 0b0) { setTurn(Turn.User); }
            else { setTurn(Turn.Response); }
            setMode(mode);
        }
        public Board(int match) {
            Data = 0;
            setCase(match);
        }
        public ArrayList<Integer> locateChess(Chess chess) {
            ArrayList<Integer> Rst = new ArrayList<Integer>(9);
            for (int i = 1; i <= 9; ++i) {
                if (get(i) == chess) { Rst.add(i); }
            }
            return Rst;
        }
        public Board[] parseState(int state)
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
            for (int i = 0; i < Sz; ++i) {
                Rst[i] = clone();
            }
            int Dx = 1;
            if (C1) {
                int Dy = Dx;
                Dx *= 2;
                for (int i = Dy; i < Sz; ++i) {
                    if (i % Dx == 0) { i += Dy; }
                    Rst[i].setParse1(!V1);
                }
            }
            if (C2) {
                int Dy = Dx;
                Dx *= 2;
                for (int i = Dy; i < Sz; ++i) {
                    if (i % Dx == 0) { i += Dy; }
                    Rst[i].setParse2(!V2);
                }
            }
            if (C4) {
                int Dy = Dx;
                Dx *= 2;
                for (int i = Dy; i < Sz; ++i) {
                    if (i % Dx == 0) { i += Dy; }
                    Rst[i].setParse4(!V4);
                }
            }
            if (C8) {
                int Dy = Dx;
                Dx *= 2;
                for (int i = Dy; i < Sz; ++i) {
                    if (i % Dx == 0) { i += Dy; }
                    Rst[i].setParse8(!V8);
                }
            }
            return Rst;
        }
        @Override
        public String toString() {
            StringBuilder Rst = new StringBuilder(100);
            Rst.append("Board < Mode.");
            Rst.append(getMode().toString());
            Rst.append(", Turn.");
            Rst.append(getTurn().toString());
            Rst.append(", Result.");
            Rst.append(getResult().toString());
            Rst.append(" > { 0x");
            Rst.append(Integer.toHexString(getRound()).toUpperCase());
            Rst.append(" } [ ");
            for (int i = 1; i <= 9; ++i) {
                if (get(i) == Chess.None) { Rst.append("_"); }
                else if (get(i) == Chess.X) { Rst.append("X"); }
                else if (get(i) == Chess.O) { Rst.append("O"); }
                else if (get(i) == Chess.Preferred) { Rst.append("+"); }
                if (i == 3 || i == 6) { Rst.append(", "); }
            }
            Rst.append(" ] ( 0b");
            Rst.append(String.format("%4s", Integer.toBinaryString(getState())).replace(" ", "0"));
            Rst.append(", ");
            Rst.append(getParse8() ? "↓" : "↑");
            Rst.append(Integer.toString(getMoves() * 45));
            Rst.append("°, Orientation.");
            Rst.append(getOrient().toString());
            Rst.append(" )");
            return Rst.toString();
        }
        @Override
        public Board clone() {
            Board Rst = new Board(Mode.Attacker);
            Rst.Data = Data;
            return Rst;
        }
        public void rotate(int moves) {
            moves %= 8;
            if (moves < 0) { moves += 8; }
            int Nears = Data & Circle;
            Nears <<= moves * 2;
            Nears |= Nears >>> 16;
            Data &= ~Circle;
            Data |= Nears & Circle;
        }
        public void reflect(Orientation Orient) {
            if (Orient == Orientation.Horizontal) {
                int Lines = getCase();
                int Fst3 = Lines >>> 16;
                int Lst3 = Lines << 16;
                Lines &= Center;
                Lines |= Fst3;
                Lines |= Lst3;
                setCase(Lines);
            } else if (Orient == Orientation.Upward) {
                rotate(-1);
                reflect(Orientation.Horizontal);
                rotate(1);
            } else if (Orient == Orientation.Vertical) {
                rotate(-2);
                reflect(Orientation.Horizontal);
                rotate(2);
            } else if (Orient == Orientation.Downward) {
                rotate(-3);
                reflect(Orientation.Horizontal);
                rotate(3);
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
        private final Board Refer;
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
            Data = Source & Field;
            Refer = new Board(Data);
            Parses = Refer.parseState(Data >>> 24);
        }
        @Override
        public String toString() {
            StringBuilder Rst = new StringBuilder(100);
            Rst.append("Pack [ ");
            for (int i = 1; i <= 9; ++i)
            {
                if (Refer.get(i) == Chess.None) { Rst.append("_"); }
                else if (Refer.get(i) == Chess.X) { Rst.append("X"); }
                else if (Refer.get(i) == Chess.O) { Rst.append("O"); }
                else if (Refer.get(i) == Chess.Preferred) { Rst.append("+"); }
                if (i == 3 || i == 6) { Rst.append(", "); }
            }
            Rst.append(" ] ( 0b");
            Rst.append(String.format("%4s", Integer.toBinaryString(Data >>> 24)).replace(" ", "0"));
            Rst.append(" )");
            return Rst.toString();
        }
    }
    private static class Boxes {
        private final int Box = 0b11;
        private int Data;
        public int get(int i) {
            i %= 16;
            if (i < 0) { i += 16; }
            return (Data >>> (i * 2)) & Box;
        }
        public void set(int i, int value) {
            i %= 16;
            if (i < 0) { i += 16; }
            Data &= ~(Box << (i * 2));
            Data |= (value & Box) << (i * 2);
        }
        public int getValues() {
            return Data;
        }
        public Boxes(int Values) {
            Data = Values;
        }
    }
    private static class Tuple {
        public final int Data;
        public final Pack Won;
        public final Pack Lost;
        public final Pack Mask;
        public Tuple(int Code) {
            Data = Code & Field;
            Boxes BWon = new Boxes(Code);
            Boxes BLost = new Boxes(Code);
            Boxes BMask = new Boxes(Code);
            for (int i = 0; i <= 10; ++i)
            {
                if (BWon.get(i) == 0b01) { BWon.set(i, 0b00); }
                if (BLost.get(i) == 0b01) { BLost.set(i, 0b00); }
                else if (BLost.get(i) == 0b10) { BLost.set(i, 0b01); }
                if (BMask.get(i) == 0b01) { BMask.set(i, 0b00); }
                else { BMask.set(i, 0b11); }
            }
            Won = new Pack((BWon.getValues() & Match) | (Code & Parse));
            Lost = new Pack((BLost.getValues() & Match) | (Code & Parse));
            Mask = new Pack((BMask.getValues() & Match) | (Code & Parse));
        }
        @Override
        public String toString() {
            Boxes BData = new Boxes(Data);
            StringBuilder Rst = new StringBuilder(100);
            Rst.append("Tuple [ ");
            for (int i = 10; i >= 0; --i)
            {
                if (i == 3 || i == 7) { Rst.append(", "); }
                else if (BData.get(i) == 0b00) { Rst.append("_"); }
                else if (BData.get(i) == 0b01) { Rst.append("~"); }
                else if (BData.get(i) == 0b10) { Rst.append("$"); }
                else if (BData.get(i) == 0b11) { Rst.append("+"); }
            }
            Rst.append(" ] ( 0b");
            Rst.append(String.format("%4s", Integer.toBinaryString(Data >>> 24)).replace(" ", "0"));
            Rst.append(" )");
            return Rst.toString();
        }
    }
    private static final Tuple[] ZeroSurvive = new Tuple[] {
        new Tuple(0b0011_00011001_00011001_00011001),
        new Tuple(0b0110_00100101_00100101_00100101),
    };
    private static final Tuple[] SingleSurvive = new Tuple[] {
        new Tuple(0b0011_00011001_00011101_00011001),
        new Tuple(0b0111_00011001_00011001_00011101),
        new Tuple(0b0110_00100101_00110101_00100101),
        new Tuple(0b1110_00100101_00100101_00110101),
    };
    private static final Tuple[] DoubleSurvive = new Tuple[] {
        new Tuple(0b1110_00010111_00011010_00000100),
        new Tuple(0b1110_00010111_00011000_00000110),
        new Tuple(0b1110_00010111_00010010_00100100),
        new Tuple(0b1110_00010111_00010000_00100110),
        new Tuple(0b0110_00001011_00010110_00010100),
        new Tuple(0b0110_00100011_00010110_00010100),
        new Tuple(0b0110_00001011_00010100_00010110),
        new Tuple(0b0110_00100011_00010100_00010110),
    };
    private static final Pack[] Cases = new Pack[] {
        new Pack(0b0000_00110011_00001100_00110011),
        new Pack(0b0000_00110011_00001000_00110011),
        new Pack(0b0111_00001000_00001100_00000000),
    };
    private static final Pack MaskA = new Pack(0b1111_00111111_00111111_00111111);
    private final Container[] Co;
    private Mode LstMo;
    private String getShownText() {
        String Rst = Bo.inDebugForm() ? "< Debug > " : "";
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
        if (value == Result.Empty) {
            Bo = new Board(Bo.getMode());
        } else {
            Bo.setTurn(Turn.Terminated);
            Bo.setRound(9);
        }
        Bo.setResult(value);
        setTitle(getShownText());
    }
    private static final Color WindowStatic = new Color(0xF0F0F0);
    private static final Color ControlStatic = new Color(0xFDFDFD);
    private static final Color ControlHover = new Color(0xE0EEF9);
    private static final Color BorderStatic = new Color(0xD0D0D0);
    private static final Color BorderHover = new Color(0x0078D4);
    private static final Color ChessO = new Color(0x008000);
    private static final Color ChessX = new Color(0xFF0000);
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
        Button1.setBackground(ControlStatic);
        Button2.setBackground(ControlStatic);
        Button3.setBackground(ControlStatic);
        Button4.setBackground(ControlStatic);
        Button5.setBackground(ControlStatic);
        Button6.setBackground(ControlStatic);
        Button7.setBackground(ControlStatic);
        Button8.setBackground(ControlStatic);
        Button9.setBackground(ControlStatic);
        ButtonSwitch.setBackground(ControlStatic);
        ButtonReset.setBackground(ControlStatic);
        Panel.setBackground(WindowStatic);
        Button1.setBorder(new LineBorder(BorderStatic));
        Button2.setBorder(new LineBorder(BorderStatic));
        Button3.setBorder(new LineBorder(BorderStatic));
        Button4.setBorder(new LineBorder(BorderStatic));
        Button5.setBorder(new LineBorder(BorderStatic));
        Button6.setBorder(new LineBorder(BorderStatic));
        Button7.setBorder(new LineBorder(BorderStatic));
        Button8.setBorder(new LineBorder(BorderStatic));
        Button9.setBorder(new LineBorder(BorderStatic));
        ButtonSwitch.setBorder(new LineBorder(BorderStatic));
        ButtonReset.setBorder(new LineBorder(BorderStatic));
        Button1.setFocusPainted(false);
        Button2.setFocusPainted(false);
        Button3.setFocusPainted(false);
        Button4.setFocusPainted(false);
        Button5.setFocusPainted(false);
        Button6.setFocusPainted(false);
        Button7.setFocusPainted(false);
        Button8.setFocusPainted(false);
        Button9.setFocusPainted(false);
        ButtonSwitch.setFocusPainted(false);
        ButtonReset.setFocusPainted(false);
        Button1.setFont(new Font("Consoles", Font.BOLD, 20));
        Button2.setFont(new Font("Consoles", Font.BOLD, 20));
        Button3.setFont(new Font("Consoles", Font.BOLD, 20));
        Button4.setFont(new Font("Consoles", Font.BOLD, 20));
        Button5.setFont(new Font("Consoles", Font.BOLD, 20));
        Button6.setFont(new Font("Consoles", Font.BOLD, 20));
        Button7.setFont(new Font("Consoles", Font.BOLD, 20));
        Button8.setFont(new Font("Consoles", Font.BOLD, 20));
        Button9.setFont(new Font("Consoles", Font.BOLD, 20));
        ButtonSwitch.setFont(new Font("Dialog", Font.BOLD, 12));
        ButtonReset.setFont(new Font("Dialog", Font.BOLD, 12));
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
        setName("MainWindow");
        setResizable(false);
        setTitle("TicTacToe");
        setLocationRelativeTo(null);
        setIconImage(new ImageIcon(Objects.requireNonNull(getClass().getResource("/TicTacToe.png"))).getImage());
        addWindowListener(new MainListener());
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
    public MainWindow() {
        super((JDialog)null);
        initializeComponent();
        LstMo = Mode.StartupMode;
        Bo = new Board(Mode.Attacker);
        Co = new Container[] { this, Button1, Button2, Button3, Button4, Button5, Button6, Button7, Button8, Button9, ButtonSwitch, ButtonReset };
    }
    public static void runLoop(MainWindow form) {
        form.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        form.setVisible(true);
    }
    private void chooseChess(ArrayList<Integer> chosen) {
        putChess(Co[chosen.get(new Random().nextInt(chosen.size()))]);
    }
    private boolean processResponse(Board[] match, Board[] mask) {
        for (int i = 0; i < match.length; ++i) {
            if ((Bo.getCase() & mask[i].getCase()) == match[i].getSanitizer().getCase()) {
                chooseChess(match[i].locateChess(Chess.Preferred));
                return true;
            }
        }
        return false;
    }
    private void checkResponse() {
        for (Pack P : Cases) {
            if (processResponse(P.getBoards(), MaskA.getBoards())) { return; }
        }
        for (Tuple T : SingleSurvive)
        {
            if (processResponse(T.Lost.getBoards(), T.Mask.getBoards())) { return; }
        }
        for (Tuple T : SingleSurvive)
        {
            if (processResponse(T.Won.getBoards(), T.Mask.getBoards())) { return; }
        }
        for (Tuple T : DoubleSurvive)
        {
            if (processResponse(T.Lost.getBoards(), T.Mask.getBoards())) { return; }
        }
        for (Tuple T : DoubleSurvive)
        {
            if (processResponse(T.Won.getBoards(), T.Mask.getBoards())) { return; }
        }
        chooseChess(Bo.locateChess(Chess.None));
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
        if (getRe() != Result.Empty) { return; }
        for (Tuple T : ZeroSurvive)
        {
            if (processResult(T.Lost.getBoards(), T.Mask.getBoards(), Result.Lost)) { return; }
        }
        for (Tuple T : ZeroSurvive)
        {
            if (processResult(T.Won.getBoards(), T.Mask.getBoards(), Result.Won)) { return; }
        }
        if (Bo.getRound() == 9) { setRe(Result.Tied); }
    }
    private void newGame(Mode mode) {
        if (mode == Mode.StartupMode || getMo() == mode) { setTu(Turn.Unspecified); }
        else if (mode == Mode.SwitchSide) { setMo(Bo.getConjugateSide()); }
        else if (mode == Mode.SwitchForm) { setMo(Bo.getConjugateForm()); }
        else if (mode == Mode.BonusScene) {
            if (LstMo == Mode.StartupMode) { LstMo = getMo(); }
            if (getMo() == Mode.DebugAttacker) { setTu(Turn.Unspecified); }
            else { setMo(Mode.DebugAttacker); }
        } else if (mode == Mode.ClumsyScene) {
            if (LstMo == Mode.StartupMode) { LstMo = getMo(); }
            if (getMo() == Mode.DebugDefender) { setTu(Turn.Unspecified); }
            else { setMo(Mode.DebugDefender); }
        } else { setMo(mode); }
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
        } else if (Bo.onDefenderSide() && !Bo.inDebugForm()) { checkResponse(); }
    }
    private void putChess(Container target) {
        ButtonReset.setEnabled(true);
        int i = Arrays.asList(Co).indexOf(target);
        if (Bo.get(i) == Chess.None && getRe() == Result.Empty) {
            if (getTu() == Turn.User) {
                Bo.set(i, Chess.O);
                ((AbstractButton)target).setText(" O ");
                target.setForeground(ChessO);
                setTu(Turn.Response);
                checkResult();
                if (!Bo.inDebugForm() && getRe() == Result.Empty)
                {
                    checkResponse();
                    checkResult();
                }
            } else if (getTu() == Turn.Response) {
                Bo.set(i, Chess.X);
                ((AbstractButton)target).setText(" X ");
                target.setForeground(ChessX);
                setTu(Turn.User);
                checkResult();
            }
        }
    }
    private void buttonSwitchClick(Object sender, ActionEvent e) {
        if (LstMo != Mode.StartupMode) {
            newGame(Bo.onDefenderSide() ? Mode.BonusScene : Mode.ClumsyScene);
        } else {
            newGame(Mode.SwitchSide);
        }
    }
    private void buttonResetClick(Object sender, ActionEvent e) {
        if (LstMo != Mode.StartupMode) {
            Mode Mo = LstMo;
            LstMo = Mode.StartupMode;
            newGame(Mo);
        } else {
            newGame(Mode.StartupMode);
        }
    }
    private void mainWindowLoad(Object sender, WindowEvent e) {
        newGame(Mode.StartupMode);
    }
    private void buttonChessKeyDown(Object sender, KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_W) {
            newGame(Mode.BonusScene);
        } else if (e.getKeyCode() == KeyEvent.VK_L) {
            newGame(Mode.ClumsyScene);
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE && LstMo != Mode.StartupMode) {
            Mode Mo = LstMo;
            LstMo = Mode.StartupMode;
            newGame(Mo);
        } else if (e.getKeyCode() == KeyEvent.VK_D && !Bo.inDebugForm()) {
            newGame(Bo.getConjugateForm());
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE && Bo.inDebugForm()) {
            newGame(Bo.getConjugateForm());
        }
    }
    private void buttonChessClick(Object sender, ActionEvent e) {
        putChess((Container)sender);
    }
    private class MainListener implements WindowListener {
        @Override
        public void windowOpened(WindowEvent e) {
            mainWindowLoad(e.getSource(), e);
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
            buttonChessClick(e.getSource(), e);
        }
        @Override
        public void keyPressed(KeyEvent e) {
            buttonChessKeyDown(e.getSource(), e);
        }
        @Override
        public void keyReleased(KeyEvent e) {
        }
        @Override
        public void keyTyped(KeyEvent e) {
        }
        @Override
        public void focusGained(FocusEvent e) {
            JComponent sender = (JComponent)e.getSource();
            sender.setBorder(new LineBorder(BorderHover));
        }
        @Override
        public void focusLost(FocusEvent e) {
            JComponent sender = (JComponent)e.getSource();
            sender.setBorder(new LineBorder(BorderStatic));
        }
        @Override
        public void mouseClicked(MouseEvent e) {
        }
        @Override
        public void mousePressed(MouseEvent e) {
        }
        @Override
        public void mouseReleased(MouseEvent e) {
            JComponent sender = (JComponent)e.getSource();
            if (!sender.isEnabled()) {
                sender.setBackground(ControlStatic);
            }
        }
        @Override
        public void mouseEntered(MouseEvent e) {
            JComponent sender = (JComponent)e.getSource();
            if (sender.isEnabled()) {
                sender.setBackground(ControlHover);
            }
        }
        @Override
        public void mouseExited(MouseEvent e) {
            JComponent sender = (JComponent)e.getSource();
            if (sender.isEnabled()) {
                sender.setBackground(ControlStatic);
            }
        }
    }
    private class SwitchListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            buttonSwitchClick(e.getSource(), e);
        }
    }
    private class ResetListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            buttonResetClick(e.getSource(), e);
        }
    }
}

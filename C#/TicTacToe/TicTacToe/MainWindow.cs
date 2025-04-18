/*
 *   TicTacToe
 *   
 *   A game you can be an Attacker or Defender, as a user you may put an O
 *   chess while the program might response from an X chess. Which of the
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
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
namespace TicTacToe
{
    public partial class MainWindow : Form
    {
        #region helper-classes
        private enum Mode : uint
        {
            Attacker = 0,
            Defender = 1,
            DebugAttacker = 2,
            DebugDefender = 3,
            StartupMode = 4,
            ConjugateMode = 5,
            ConfigureMode = 6,
            BonusScene = 7,
            ClumsyScene = 8,
        }
        private enum Turn : uint
        {
            Unspecified = 0,
            User = 1,
            Response = 2,
            Terminated = 3,
        }
        private enum Result : uint
        {
            Empty = 0,
            Won = 1,
            Lost = 2,
            Tied = 3,
        }
        private enum Chess : uint
        {
            None = 0,
            X = 1,
            O = 2,
            Preferred = 3,
        }
        private enum Orientation : uint
        {
            Horizontal = 0,
            Upward = 1,
            Vertical = 2,
            Downward = 3,
        }
        private struct Board
        {
            private const uint MaskFst3 = 0x3F00u;
            private const uint Box = 0b11u;
            private const uint Conj = 0b1u;
            private const uint Conf = 0b10u;
            private const uint P1 = 0b1u;
            private const uint P2 = 0b10u;
            private const uint P4 = 0b100u;
            private const uint P8 = 0b1000u;
            private const int IRound = 0;
            private const int IMode = 10;
            private const int ITurn = 11;
            private const int IResult = 12;
            private const int IState = 13;
            private static readonly int[] Offset;
            private static readonly uint[] Mask;
            static Board()
            {
                Offset = new int[] { 20, 12, 10, 8, 14, 24, 6, 0, 2, 4, 30, 28, 26, 16 };
                Mask = new uint[Offset.Length];
                Mask[IRound] = 0xF00000u;
                Mask[IState] = 0xF0000u;
                for (int i = 1; i < Offset.Length - 1; ++i)
                {
                    Mask[i] = Box << Offset[i];
                }
            }
            private uint Data;
            public Chess this[int i]
            {
                get
                {
                    if (i < 1 || i > 9) { throw new IndexOutOfRangeException(); }
                    return (Chess)((Data & Mask[i]) >> Offset[i]);
                }
                set
                {
                    if (i < 1 || i > 9) { throw new IndexOutOfRangeException(); }
                    Data &= ~Mask[i];
                    Data |= ((uint)value << Offset[i]) & Mask[i];
                }
            }
            public Mode Mode
            {
                get
                {
                    return (Mode)((Data & Mask[IMode]) >> Offset[IMode]);
                }
                set
                {
                    Data &= ~Mask[IMode];
                    Data |= ((uint)value << Offset[IMode]) & Mask[IMode];
                }
            }
            public Mode ConjugateMode
            {
                get
                {
                    return (Mode)(((uint)Mode & Conf) | (~(uint)Mode & Conj));
                }
            }
            public Mode ConfigureMode
            {
                get
                {
                    return (Mode)(((uint)Mode & Conj) | (~(uint)Mode & Conf));
                }
            }
            public bool OnDefenderSide
            {
                get
                {
                    return ((uint)Mode & Conj) == Conj;
                }
            }
            public bool InDebugMode
            {
                get
                {
                    return ((uint)Mode & Conf) == Conf;
                }
            }
            public Turn Turn
            {
                get
                {
                    return (Turn)((Data & Mask[ITurn]) >> Offset[ITurn]);
                }
                set
                {
                    Data &= ~Mask[ITurn];
                    Data |= ((uint)value << Offset[ITurn]) & Mask[ITurn];
                }
            }
            public Result Result
            {
                get
                {
                    return (Result)((Data & Mask[IResult]) >> Offset[IResult]);
                }
                set
                {
                    Data &= ~Mask[IResult];
                    Data |= ((uint)value << Offset[IResult]) & Mask[IResult];
                }
            }
            public uint Round
            {
                get
                {
                    return (Data & Mask[IRound]) >> Offset[IRound];
                }
                set
                {
                    Data &= ~Mask[IRound];
                    Data |= (value << Offset[IRound]) & Mask[IRound];
                }
            }
            public uint State
            {
                get
                {
                    return (Data & Mask[IState]) >> Offset[IState];
                }
                private set
                {
                    Data &= ~Mask[IState];
                    Data |= (value << Offset[IState]) & Mask[IState];
                }
            }
            public int Moves
            {
                get
                {
                    return (int)(State & 0b111u);
                }
            }
            public Orientation Orient
            {
                get
                {
                    return (Orientation)(State & Box);
                }
            }
            public bool Parse1
            {
                get
                {
                    return (State & P1) == P1;
                }
                set
                {
                    bool Origin = (State & P1) == P1;
                    if (Origin == value) { return; }
                    if (value)
                    {
                        Rotate(1);
                        State |= P1;
                    }
                    else
                    {
                        Rotate(-1);
                        State &= ~P1;
                    }
                }
            }
            public bool Parse2
            {
                get
                {
                    return (State & P2) == P2;
                }
                set
                {
                    bool Origin = (State & P2) == P2;
                    if (Origin == value) { return; }
                    if (value)
                    {
                        Rotate(2);
                        State |= P2;
                    }
                    else
                    {
                        Rotate(-2);
                        State &= ~P2;
                    }
                }
            }
            public bool Parse4
            {
                get
                {
                    return (State & P4) == P4;
                }
                set
                {
                    bool Origin = (State & P4) == P4;
                    if (Origin == value) { return; }
                    if (value)
                    {
                        Rotate(4);
                        State |= P4;
                    }
                    else
                    {
                        Rotate(-4);
                        State &= ~P4;
                    }
                }
            }
            public bool Parse8
            {
                get
                {
                    return (State & P8) == P8;
                }
                set
                {
                    bool Origin = (State & P8) == P8;
                    if (Origin == value) { return; }
                    Reflect(Orient);
                    if (value) { State |= P8; }
                    else { State &= ~P8; }
                }
            }
            public uint Case
            {
                get
                {
                    uint Rst = (Data & MaskFst3) >> 6;
                    for (int i = 4; i <= 9; ++i)
                    {
                        Rst <<= i == 7 ? 4 : 2;
                        Rst |= (Data & Mask[i]) >> Offset[i];
                    }
                    return Rst;
                }
                set
                {
                    Data &= ~MaskFst3;
                    Data |= (value >> 8) & MaskFst3;
                    uint Rest = value & 0xFFFFu;
                    for (int i = 9; i >= 4; --i)
                    {
                        Data &= ~Mask[i];
                        Data |= (Rest & Box) << Offset[i];
                        Rest >>= i == 7 ? 4 : 2;
                    }
                }
            }
            public Board Sanitizer
            {
                get
                {
                    Board Rst = this;
                    for (int i = 1; i <= 9; ++i)
                    {
                        if (Rst[i] == Chess.Preferred) { Rst[i] = Chess.None; }
                    }
                    return Rst;
                }
            }
            public Board(Mode Mode)
            {
                Data = 0u;
                if (((uint)Mode & Conj) == 0b0u) { Turn = Turn.User; }
                else { Turn = Turn.Response; }
                this.Mode = Mode;
            }
            public Board(uint Case)
            {
                Data = 0u;
                this.Case = Case;
            }
            public List<int> LocateChess(Chess Chess)
            {
                List<int> Rst = new List<int>(9);
                for (int i = 1; i <= 9; ++i)
                {
                    if (this[i] == Chess) { Rst.Add(i); }
                }
                return Rst;
            }
            public Board[] ParseState(uint State)
            {
                bool C1 = (State & P1) == P1;
                bool C2 = (State & P2) == P2;
                bool C4 = (State & P4) == P4;
                bool C8 = (State & P8) == P8;
                bool V1 = Parse1;
                bool V2 = Parse2;
                bool V4 = Parse4;
                bool V8 = Parse8;
                int Sz = 1;
                if (C1) { Sz *= 2; }
                if (C2) { Sz *= 2; }
                if (C4) { Sz *= 2; }
                if (C8) { Sz *= 2; }
                Board[] Rst = new Board[Sz];
                for (int i = 0; i < Sz; ++i)
                {
                    Rst[i] = this;
                }
                int Dx = 1;
                if (C1)
                {
                    int Dy = Dx;
                    Dx *= 2;
                    for (int i = Dy; i < Sz; ++i)
                    {
                        if (i % Dx == 0) { i += Dy; }
                        Rst[i].Parse1 = !V1;
                    }
                }
                if (C2)
                {
                    int Dy = Dx;
                    Dx *= 2;
                    for (int i = Dy; i < Sz; ++i)
                    {
                        if (i % Dx == 0) { i += Dy; }
                        Rst[i].Parse2 = !V2;
                    }
                }
                if (C4)
                {
                    int Dy = Dx;
                    Dx *= 2;
                    for (int i = Dy; i < Sz; ++i)
                    {
                        if (i % Dx == 0) { i += Dy; }
                        Rst[i].Parse4 = !V4;
                    }
                }
                if (C8)
                {
                    int Dy = Dx;
                    Dx *= 2;
                    for (int i = Dy; i < Sz; ++i)
                    {
                        if (i % Dx == 0) { i += Dy; }
                        Rst[i].Parse8 = !V8;
                    }
                }
                return Rst;
            }
            public override string ToString()
            {
                StringBuilder Rst = new StringBuilder(100);
                Rst.Append("Board < Mode.");
                Rst.Append(Mode.ToString());
                Rst.Append(", Turn.");
                Rst.Append(Turn.ToString());
                Rst.Append(", Result.");
                Rst.Append(Result.ToString());
                Rst.Append(" > { 0x");
                Rst.Append(Convert.ToString(Round, 16).ToUpper());
                Rst.Append(" } [ ");
                for (int i = 1; i <= 9; ++i)
                {
                    if (this[i] == Chess.None) { Rst.Append("_"); }
                    else if (this[i] == Chess.X) { Rst.Append("X"); }
                    else if (this[i] == Chess.O) { Rst.Append("O"); }
                    else if (this[i] == Chess.Preferred) { Rst.Append("+"); }
                    if (i == 3 || i == 6) { Rst.Append(", "); }
                }
                Rst.Append(" ] ( 0b");
                Rst.Append(Convert.ToString(State, 2).PadLeft(4, '0'));
                Rst.Append(", ");
                Rst.Append(Parse8 ? "↓" : "↑");
                Rst.Append((Moves * 45).ToString());
                Rst.Append("°, Orientation.");
                Rst.Append(Orient.ToString());
                Rst.Append(" )");
                return Rst.ToString();
            }
            public void Rotate(int Moves)
            {
                Moves %= 8;
                if (Moves < 0) { Moves += 8; }
                uint Nears = (Data & 0xFFFFu) << (Moves * 2);
                Nears |= (Nears & 0xFFFF0000u) >> 16;
                Data = (Data & 0xFFFF0000u) | (Nears & 0xFFFFu);
            }
            public void Reflect(Orientation Orient)
            {
                if (Orient == Orientation.Horizontal)
                {
                    uint Lines = Case;
                    uint First = (Lines & 0xFF0000u) >> 16;
                    uint Last = (Lines & 0xFFu) << 16;
                    Lines &= 0xFF00u;
                    Lines |= First;
                    Lines |= Last;
                    Case = Lines;
                }
                else if (Orient == Orientation.Upward)
                {
                    Rotate(-1);
                    Reflect(Orientation.Horizontal);
                    Rotate(1);
                }
                else if (Orient == Orientation.Vertical)
                {
                    Rotate(-2);
                    Reflect(Orientation.Horizontal);
                    Rotate(2);
                }
                else if (Orient == Orientation.Downward)
                {
                    Rotate(-3);
                    Reflect(Orientation.Horizontal);
                    Rotate(3);
                }
            }
            public void ClearParse()
            {
                Rotate(-Moves);
                if (Parse8) { Reflect(Orientation.Horizontal); }
                State = 0u;
            }
            public void Reset()
            {
                Data = 0u;
            }
        }
        private readonly struct Pack
        {
            private readonly uint Data;
            private readonly Board Refer;
            private readonly Board[] Parses;
            public Board[] Boards
            {
                get
                {
                    return Parses.ToArray();
                }
            }
            public uint Source
            {
                get
                {
                    return Data;
                }
            }
            public Pack(uint Source)
            {
                Data = Source & 0xF3F3F3Fu;
                Refer = new Board(Data);
                Parses = Refer.ParseState(Data >> 24);
            }
            public override string ToString()
            {
                StringBuilder Rst = new StringBuilder(100);
                Rst.Append("Pack [ ");
                for (int i = 1; i <= 9; ++i)
                {
                    if (Refer[i] == Chess.None) { Rst.Append("_"); }
                    else if (Refer[i] == Chess.X) { Rst.Append("X"); }
                    else if (Refer[i] == Chess.O) { Rst.Append("O"); }
                    else if (Refer[i] == Chess.Preferred) { Rst.Append("+"); }
                    if (i == 3 || i == 6) { Rst.Append(", "); }
                }
                Rst.Append(" ] ( 0b");
                Rst.Append(Convert.ToString(Data >> 24, 2).PadLeft(4, '0'));
                Rst.Append(" )");
                return Rst.ToString();
            }
        }
        private struct Boxes
        {
            private const uint Box = 0b11u;
            private uint Data;
            public uint this[int i]
            {
                get
                {
                    i %= 16;
                    if (i < 0) { i += 16; }
                    return (Data >> (i * 2)) & Box;
                }
                set
                {
                    i %= 16;
                    if (i < 0) { i += 16; }
                    Data &= ~(Box << (i * 2));
                    Data |= (value & Box) << (i * 2);
                }
            }
            public uint Values
            {
                get
                {
                    return Data;
                }
            }
            public Boxes(uint Values)
            {
                Data = Values;
            }
        }
        private readonly struct Tuple
        {
            public readonly uint Data;
            public readonly Pack Won;
            public readonly Pack Lost;
            public readonly Pack Mask;
            public Tuple(uint Code)
            {
                Data = Code & 0xF3F3F3Fu;
                Boxes BWon = new Boxes(Code);
                Boxes BLost = new Boxes(Code);
                Boxes BMask = new Boxes(Code);
                for (int i = 0; i <= 10; ++i)
                {
                    if (BWon[i] == 0b01u) { BWon[i] = 0b00u; }
                    if (BLost[i] == 0b01u) { BLost[i] = 0b00u; }
                    else if (BLost[i] == 0b10u) { BLost[i] = 0b01u; }
                    if (BMask[i] == 0b01u) { BMask[i] = 0b00u; }
                    else { BMask[i] = 0b11u; }
                }
                Won = new Pack((BWon.Values & 0x3F3F3Fu) | (Code & 0xF000000u));
                Lost = new Pack((BLost.Values & 0x3F3F3Fu) | (Code & 0xF000000u));
                Mask = new Pack((BMask.Values & 0x3F3F3Fu) | (Code & 0xF000000u));
            }
            public override string ToString()
            {
                Boxes BData = new Boxes(Data);
                StringBuilder Rst = new StringBuilder(100);
                Rst.Append("Tuple [ ");
                for (int i = 10; i >= 0; --i)
                {
                    if (i == 3 || i == 7) { Rst.Append(", "); }
                    else if (BData[i] == 0b00u) { Rst.Append("_"); }
                    else if (BData[i] == 0b01u) { Rst.Append("~"); }
                    else if (BData[i] == 0b10u) { Rst.Append("$"); }
                    else if (BData[i] == 0b11u) { Rst.Append("+"); }
                }
                Rst.Append(" ] ( 0b");
                Rst.Append(Convert.ToString(Data >> 24, 2).PadLeft(4, '0'));
                Rst.Append(" )");
                return Rst.ToString();
            }
        }
        #endregion
        #region constants
        private static readonly Tuple[] ZeroSurvive;
        private static readonly Tuple[] SingleSurvive;
        private static readonly Tuple[] DoubleSurvive;
        private static readonly Pack[] Cases;
        private static readonly Pack MaskA;
        static MainWindow()
        {
            ZeroSurvive = new Tuple[] {
                new Tuple(0b0011_00011001_00011001_00011001u),
                new Tuple(0b0110_00100101_00100101_00100101u),
            };
            SingleSurvive = new Tuple[] {
                new Tuple(0b0011_00011001_00011101_00011001u),
                new Tuple(0b0111_00011001_00011001_00011101u),
                new Tuple(0b0110_00100101_00110101_00100101u),
                new Tuple(0b1110_00100101_00100101_00110101u),
            };
            DoubleSurvive = new Tuple[] {
                new Tuple(0b1110_00010111_00011010_00000100u),
                new Tuple(0b1110_00010111_00011000_00000110u),
                new Tuple(0b1110_00010111_00010010_00100100u),
                new Tuple(0b1110_00010111_00010000_00100110u),
                new Tuple(0b0110_00001011_00010110_00010100u),
                new Tuple(0b0110_00100011_00010110_00010100u),
                new Tuple(0b0110_00001011_00010100_00010110u),
                new Tuple(0b0110_00100011_00010100_00010110u),
            };
            Cases = new Pack[] {
                new Pack(0b0000_00110011_00001100_00110011u),
                new Pack(0b0000_00110011_00001000_00110011u),
                new Pack(0b0111_00001000_00001100_00000000u),
            };
            MaskA = new Pack(0b1111_00111111_00111111_00111111u);
        }
        #endregion
        #region fields
        private readonly Control[] Co;
        private Mode LstMo;
        private string Title
        {
            get
            {
                string Rst = Bo.InDebugMode ? "< Debug > " : "";
                if (LstMo != Mode.StartupMode) { Rst = Bo.OnDefenderSide ? "< Clumsy > " : "< Bonus > "; }
                Rst += "TicTacToe";
                Rst += Bo.OnDefenderSide ? " Defender" : " Attacker";
                if (Bo.Result == Result.Won) { Rst += " [ Win ]"; }
                else if (Bo.Result == Result.Lost) { Rst += " [ Lost ]"; }
                else if (Bo.Result == Result.Tied) { Rst += " [ Tied ]"; }
                return Rst;
            }
        }
        private Board Bo;
        private Mode Mo
        {
            get
            {
                return Bo.Mode;
            }
            set
            {
                if (Bo.Mode == value) { return; }
                Bo = new Board(value);
                Text = Title;
            }
        }
        private Turn Tu
        {
            get
            {
                return Bo.Turn;
            }
            set
            {
                if (value == Turn.Unspecified)
                {
                    Bo = new Board(Bo.Mode);
                }
                else if (value == Turn.Terminated || Bo.Turn == Turn.Terminated)
                {
                    Bo.Turn = Turn.Terminated;
                    Bo.Round = 9u;
                }
                else if (Bo.Turn != value && Bo.Round < 9u)
                {
                    Bo.Turn = value;
                    ++Bo.Round;
                }
                Text = Title;
            }
        }
        private Result Re
        {
            get
            {
                return Bo.Result;
            }
            set
            {
                if (Bo.Result == value) { return; }
                if (value == Result.Empty)
                {
                    Bo = new Board(Bo.Mode);
                }
                else
                {
                    Bo.Turn = Turn.Terminated;
                    Bo.Round = 9u;
                }
                Bo.Result = value;
                Text = Title;
            }
        }
        #endregion
        #region constructors-and-methods
        public MainWindow()
        {
            InitializeComponent();
            LstMo = Mode.StartupMode;
            Co = new Control[] { this, Button1, Button2, Button3, Button4, Button5, Button6, Button7, Button8, Button9, ButtonSwitch, ButtonReset };
        }
        private void ChooseChess(List<int> Chosen)
        {
            PutChess(Co[Chosen[new Random().Next(Chosen.Count)]]);
        }
        private bool ProcessResponse(Board[] Case, Board[] Mask)
        {
            for (int i = 0; i < Case.Length; ++i)
            {
                if ((Bo.Case & Mask[i].Case) == Case[i].Sanitizer.Case)
                {
                    ChooseChess(Case[i].LocateChess(Chess.Preferred));
                    return true;
                }
            }
            return false;
        }
        private void CheckResponse()
        {
            foreach (Pack P in Cases)
            {
                if (ProcessResponse(P.Boards, MaskA.Boards)) { return; }
            }
            foreach (Tuple T in SingleSurvive)
            {
                if (ProcessResponse(T.Lost.Boards, T.Mask.Boards)) { return; }
            }
            foreach (Tuple T in SingleSurvive)
            {
                if (ProcessResponse(T.Won.Boards, T.Mask.Boards)) { return; }
            }
            foreach (Tuple T in DoubleSurvive)
            {
                if (ProcessResponse(T.Lost.Boards, T.Mask.Boards)) { return; }
            }
            foreach (Tuple T in DoubleSurvive)
            {
                if (ProcessResponse(T.Won.Boards, T.Mask.Boards)) { return; }
            }
            ChooseChess(Bo.LocateChess(Chess.None));
        }
        private bool ProcessResult(Board[] Case, Board[] Mask, Result Result)
        {
            for (int i = 0; i < Case.Length; ++i)
            {
                if ((Bo.Case & Mask[i].Case) == Case[i].Case)
                {
                    Re = Result;
                    return true;
                }
            }
            return false;
        }
        private void CheckResult()
        {
            if (Re != Result.Empty) { return; }
            foreach (Tuple T in ZeroSurvive)
            {
                if (ProcessResult(T.Lost.Boards, T.Mask.Boards, Result.Lost)) { return; }
            }
            foreach (Tuple T in ZeroSurvive)
            {
                if (ProcessResult(T.Won.Boards, T.Mask.Boards, Result.Won)) { return; }
            }
            if (Bo.Round == 9u) { Re = Result.Tied; }
        }
        private void NewGame(Mode Mode)
        {
            if (Mode == Mode.StartupMode || Mo == Mode) { Tu = Turn.Unspecified; }
            else if (Mode == Mode.ConjugateMode) { Mo = Bo.ConjugateMode; }
            else if (Mode == Mode.ConfigureMode) { Mo = Bo.ConfigureMode; }
            else if (Mode == Mode.BonusScene)
            {
                if (LstMo == Mode.StartupMode) { LstMo = Mo; }
                if (Mo == Mode.DebugAttacker) { Tu = Turn.Unspecified; }
                else { Mo = Mode.DebugAttacker; }
            }
            else if (Mode == Mode.ClumsyScene)
            {
                if (LstMo == Mode.StartupMode) { LstMo = Mo; }
                if (Mo == Mode.DebugDefender) { Tu = Turn.Unspecified; }
                else { Mo = Mode.DebugDefender; }
            }
            else
            {
                Mo = Mode;
            }
            ButtonReset.Enabled = false;
            for (int i = 1; i <= 9; ++i)
            {
                Co[i].Text = string.Empty;
                Co[i].ForeColor = Color.Black;
            }
            if (LstMo != Mode.StartupMode)
            {
                PutChess(Button1);
                PutChess(Button2);
                PutChess(Button3);
                PutChess(Button6);
                PutChess(Button9);
                PutChess(Button8);
                PutChess(Button7);
                PutChess(Button4);
                PutChess(Button5);
            }
            else if (Bo.OnDefenderSide && !Bo.InDebugMode) { CheckResponse(); }
        }
        private void PutChess(Control Target)
        {
            ButtonReset.Enabled = true;
            int i = Array.IndexOf(Co, Target);
            if (Bo[i] == Chess.None && Re == Result.Empty)
            {
                if (Tu == Turn.User)
                {
                    Bo[i] = Chess.O;
                    Target.Text = " O ";
                    Target.ForeColor = Color.Green;
                    Tu = Turn.Response;
                    CheckResult();
                    if (!Bo.InDebugMode && Re == Result.Empty)
                    {
                        CheckResponse();
                        CheckResult();
                    }
                }
                else if (Tu == Turn.Response)
                {
                    Bo[i] = Chess.X;
                    Target.Text = " X ";
                    Target.ForeColor = Color.Red;
                    Tu = Turn.User;
                    CheckResult();
                }
            }
        }
        #endregion
        #region event-handlers
        private void ButtonSwitchClick(object sender, EventArgs e)
        {
            if (LstMo != Mode.StartupMode)
            {
                NewGame(Bo.OnDefenderSide ? Mode.BonusScene : Mode.ClumsyScene);
            }
            else
            {
                NewGame(Mode.ConjugateMode);
            }
        }
        private void ButtonResetClick(object sender, EventArgs e)
        {
            if (LstMo != Mode.StartupMode)
            {
                Mode Mo = LstMo;
                LstMo = Mode.StartupMode;
                NewGame(Mo);
            }
            else
            {
                NewGame(Mode.StartupMode);
            }
        }
        private void MainWindowLoad(object sender, EventArgs e)
        {
            NewGame(Mode.StartupMode);
        }
        private void ButtonChessKeyDown(object sender, KeyEventArgs e)
        {
            if (e.KeyCode == Keys.W)
            {
                NewGame(Mode.BonusScene);
            }
            else if (e.KeyCode == Keys.L)
            {
                NewGame(Mode.ClumsyScene);
            }
            else if (e.KeyCode == Keys.Escape && LstMo != Mode.StartupMode)
            {
                Mode Mo = LstMo;
                LstMo = Mode.StartupMode;
                NewGame(Mo);
            }
            else if (e.KeyCode == Keys.D && !Bo.InDebugMode)
            {
                NewGame(Bo.ConfigureMode);
            }
            else if (e.KeyCode == Keys.Escape && Bo.InDebugMode)
            {
                NewGame(Bo.ConfigureMode);
            }
        }
        private void ButtonChessClick(object sender, EventArgs e)
        {
            PutChess(sender as Control);
        }
        #endregion
    }
}

using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Windows.Forms;
namespace TicTacToe
{
    public partial class MainForm : Form
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
            ConfigurateMode = 6,
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
            None = 0,
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
            private static readonly int[] Offset;
            private static readonly uint[] Mask;
            static Board()
            {
                Offset = new int[] { 20, 12, 10, 8, 14, 24, 6, 0, 2, 4, 30, 28, 26, 16 };
                Mask = new uint[Offset.Length];
                Mask[0] = 0xF00000u;
                Mask[13] = 0xF0000u;
                for (int i = 1; i < Offset.Length - 1; ++i)
                {
                    Mask[i] = 0b11u << Offset[i];
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
                    return (Mode)((Data & Mask[10]) >> Offset[10]);
                }
                set
                {
                    Data &= ~Mask[10];
                    Data |= ((uint)value << Offset[10]) & Mask[10];
                }
            }
            public Mode ConjugateMode
            {
                get
                {
                    return (Mode)(((uint)Mode & 0b10u) | (~(uint)Mode & 0b1u));
                }
            }
            public Mode ConfigurateMode
            {
                get
                {
                    return (Mode)(((uint)Mode & 0b1u) | (~(uint)Mode & 0b10u));
                }
            }
            public bool OnDefenderSide
            {
                get
                {
                    return ((uint)Mode & 0b1u) == 0b1u;
                }
            }
            public bool InDebugMode
            {
                get
                {
                    return ((uint)Mode & 0b10) == 0b10;
                }
            }
            public Turn Turn
            {
                get
                {
                    return (Turn)((Data & Mask[11]) >> Offset[11]);
                }
                set
                {
                    Data &= ~Mask[11];
                    Data |= ((uint)value << Offset[11]) & Mask[11];
                }
            }
            public Result Result
            {
                get
                {
                    return (Result)((Data & Mask[12]) >> Offset[12]);
                }
                set
                {
                    Data &= ~Mask[12];
                    Data |= ((uint)value << Offset[12]) & Mask[12];
                }
            }
            public uint Round
            {
                get
                {
                    return (Data & Mask[0]) >> Offset[0];
                }
                set
                {
                    Data &= ~Mask[0];
                    Data |= (value << Offset[0]) & Mask[0];
                }
            }
            public uint State
            {
                get
                {
                    return (Data & Mask[13]) >> Offset[13];
                }
                private set
                {
                    Data &= ~Mask[13];
                    Data |= (value << Offset[13]) & Mask[13];
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
                    return (Orientation)(State & 0b11u);
                }
            }
            public bool Parse1
            {
                get
                {
                    return (State & 0b1u) == 0b1u;
                }
                set
                {
                    bool Origin = (State & 0b1u) == 0b1u;
                    if (Origin == value) { return; }
                    if (value)
                    {
                        Rotate(1);
                        State |= 0b1u;
                    }
                    else
                    {
                        Rotate(-1);
                        State &= ~0b1u;
                    }
                }
            }
            public bool Parse2
            {
                get
                {
                    return (State & 0b10u) == 0b10u;
                }
                set
                {
                    bool Origin = (State & 0b10u) == 0b10u;
                    if (Origin == value) { return; }
                    if (value)
                    {
                        Rotate(2);
                        State |= 0b10u;
                    }
                    else
                    {
                        Rotate(-2);
                        State &= ~0b10u;
                    }
                }
            }
            public bool Parse4
            {
                get
                {
                    return (State & 0b100u) == 0b100u;
                }
                set
                {
                    bool Origin = (State & 0b100u) == 0b100u;
                    if (Origin == value) { return; }
                    if (value)
                    {
                        Rotate(4);
                        State |= 0b100u;
                    }
                    else
                    {
                        Rotate(-4);
                        State &= ~0b100u;
                    }
                }
            }
            public bool Parse8
            {
                get
                {
                    return (State & 0b1000u) == 0b1000u;
                }
                set
                {
                    bool Origin = (State & 0b1000u) == 0b1000u;
                    if (Origin == value) { return; }
                    Reflect(Orient);
                    if (value) { State |= 0b1000u; }
                    else { State &= ~0b1000u; }
                }
            }
            public uint Case
            {
                get
                {
                    uint Result = (Data & 0b11111100000000u) >> 6;
                    for (int i = 4; i <= 9; ++i)
                    {
                        Result <<= i == 7 ? 4 : 2;
                        Result |= (Data & Mask[i]) >> Offset[i];
                    }
                    return Result;
                }
                set
                {
                    Data &= ~0b11111100000000u;
                    Data |= (value >> 8) & 0b11111100000000u;
                    uint Rest = value & 0xFFFFu;
                    for (int i = 9; i >= 4; --i)
                    {
                        Data &= ~Mask[i];
                        Data |= (Rest & 0b11u) << Offset[i];
                        Rest >>= i == 7 ? 4 : 2;
                    }
                }
            }
            public Board Sanitizer
            {
                get
                {
                    Board Result = this;
                    for (int i = 1; i <= 9; ++i)
                    {
                        if (Result[i] == Chess.Preferred) { Result[i] = Chess.None; }
                    }
                    return Result;
                }
            }
            public Board(Mode Mode)
            {
                Data = 0u;
                if (((uint)Mode & 0b1u) == 0b0u) { Turn = Turn.User; }
                else { Turn = Turn.Response; }
                this.Mode = Mode;
            }
            public Board(uint Case)
            {
                Data = 0u;
                this.Case = Case;
            }
            public List<int> GetChessPos(Chess Chess)
            {
                List<int> Result = new List<int>(9);
                for (int i = 1; i <= 9; ++i)
                {
                    if (this[i] == Chess) { Result.Add(i); }
                }
                return Result;
            }
            public Board[] GetParse(uint State)
            {
                bool C1 = (State & 0b1u) == 0b1u;
                bool C2 = (State & 0b10u) == 0b10u;
                bool C4 = (State & 0b100u) == 0b100u;
                bool C8 = (State & 0b1000u) == 0b1000u;
                bool V1 = Parse1;
                bool V2 = Parse2;
                bool V4 = Parse4;
                bool V8 = Parse8;
                int Sz = 1;
                if (C1) { Sz *= 2; }
                if (C2) { Sz *= 2; }
                if (C4) { Sz *= 2; }
                if (C8) { Sz *= 2; }
                Board[] Result = new Board[Sz];
                for (int i = 0; i < Result.Length; ++i)
                {
                    Result[i].Data = Data;
                }
                int Dx = 1;
                if (C1)
                {
                    int Dy = Dx;
                    Dx *= 2;
                    for (int i = Dy; i < Result.Length; ++i)
                    {
                        if (i % Dx == 0) { i += Dy; }
                        Result[i].Parse1 = !V1;
                    }
                }
                if (C2)
                {
                    int Dy = Dx;
                    Dx *= 2;
                    for (int i = Dy; i < Result.Length; ++i)
                    {
                        if (i % Dx == 0) { i += Dy; }
                        Result[i].Parse2 = !V2;
                    }
                }
                if (C4)
                {
                    int Dy = Dx;
                    Dx *= 2;
                    for (int i = Dy; i < Result.Length; ++i)
                    {
                        if (i % Dx == 0) { i += Dy; }
                        Result[i].Parse4 = !V4;
                    }
                }
                if (C8)
                {
                    int Dy = Dx;
                    Dx *= 2;
                    for (int i = Dy; i < Result.Length; ++i)
                    {
                        if (i % Dx == 0) { i += Dy; }
                        Result[i].Parse8 = !V8;
                    }
                }
                return Result;
            }
            public override string ToString()
            {
                string Result = "[ ";
                for (int i = 1; i <= 9; ++i)
                {
                    if (this[i] == Chess.None) { Result += "?"; }
                    else if (this[i] == Chess.X) { Result += "X"; }
                    else if (this[i] == Chess.O) { Result += "O"; }
                    else if (this[i] == Chess.Preferred) { Result += "+"; }
                    if (i == 3 || i == 6) { Result += ", "; }
                }
                return Result += " ]";
            }
            public void Rotate(int Moves)
            {
                if (Moves < 0) { Moves = 8 - Moves; }
                Moves %= 8;
                uint Nears = (Data & 0xFFFF) << Moves * 2;
                Nears |= (Nears & 0xFFFF0000) >> 16;
                Data = (Data & 0xFFFF0000) | (Nears & 0xFFFF);
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
                    Rotate(2);
                    Reflect(Orientation.Horizontal);
                    Rotate(-2);
                }
                else if (Orient == Orientation.Downward)
                {
                    Rotate(1);
                    Reflect(Orientation.Horizontal);
                    Rotate(-1);
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
                Data = Source;
                Parses = new Board(Source).GetParse(Source >> 24);
            }
        }
        #endregion
        #region constants
        private static readonly Pack Mask;
        private static readonly Pack WonC;
        private static readonly Pack LostC;
        private static readonly Pack MaskC;
        private static readonly Pack WonS;
        private static readonly Pack LostS;
        private static readonly Pack MaskS;
        private static readonly Pack WonCN;
        private static readonly Pack LostCN;
        private static readonly Pack MaskCN;
        private static readonly Pack WonCM;
        private static readonly Pack LostCM;
        private static readonly Pack MaskCM;
        private static readonly Pack WonSN;
        private static readonly Pack LostSN;
        private static readonly Pack MaskSN;
        private static readonly Pack WonSM;
        private static readonly Pack LostSM;
        private static readonly Pack MaskSM;
        private static readonly Pack[] Cases;
        static MainForm()
        {
            Mask = new Pack(0b1111_00111111_00111111_00111111u);
            WonC = new Pack(0b0011_00001000_00001000_00001000u);
            LostC = new Pack(0b0011_00000100_00000100_00000100u);
            MaskC = new Pack(0b0011_00001100_00001100_00001100u);
            WonS = new Pack(0b0110_00100000_00100000_00100000u);
            LostS = new Pack(0b0110_00010000_00010000_00010000u);
            MaskS = new Pack(0b0110_00110000_00110000_00110000u);
            WonCN = new Pack(0b0111_00001000_00001000_00001100u);
            LostCN = new Pack(0b0111_00000100_00000100_00001100u);
            MaskCN = new Pack(0b0111_00001100_00001100_00001100u);
            WonCM = new Pack(0b0011_00001000_00001100_00001000u);
            LostCM = new Pack(0b0011_00000100_00001100_00000100u);
            MaskCM = new Pack(0b0011_00001100_00001100_00001100u);
            WonSN = new Pack(0b1110_00100000_00100000_00110000u);
            LostSN = new Pack(0b1110_00010000_00010000_00110000u);
            MaskSN = new Pack(0b1110_00110000_00110000_00110000u);
            WonSM = new Pack(0b0110_00100000_00110000_00100000u);
            LostSM = new Pack(0b0110_00010000_00110000_00010000u);
            MaskSM = new Pack(0b0110_00110000_00110000_00110000u);
            Cases = new Pack[] {
                new Pack(0b0000_00110011_00001100_00110011u),
                new Pack(0b0111_00001000_00001100_00000000u),
                new Pack(0b0110_00001100_00110111_00111011u),
                new Pack(0b0110_00011111_00111000_00110000u),
                new Pack(0b0110_00011100_00111111_00001110u),
                new Pack(0b1110_00011011_00110011_00111111u),
                new Pack(0b1110_00011110_00001111_00001100u),
                new Pack(0b1110_00111101_00100011_00111111u),
                new Pack(0b0010_00100011_00000100_00110010u),
                new Pack(0b0110_00001000_00100111_00001111u),
                new Pack(0b0110_00011100_00111011_00001110u),
                new Pack(0b1110_00110010_00100100_00111100u),
                new Pack(0b1110_00000010_00100101_00000011u),
                new Pack(0b1110_00001101_00110110_00101111u),
                new Pack(0b1110_00011110_00101111_00011100u),
            };
        }
        #endregion
        #region fields
        private readonly Control[] Co;
        private string Title
        {
            get
            {
                string Result = "TicTacToe";
                if (Bo.Mode == Mode.Attacker || Bo.Mode == Mode.DebugAttacker)
                {
                    Result += " Attacker";
                }
                else if (Bo.Mode == Mode.Defender || Bo.Mode == Mode.DebugDefender)
                {
                    Result += " Defender";
                }
                if (Bo.Mode == Mode.DebugAttacker || Bo.Mode == Mode.DebugDefender)
                {
                    Result = "< Debug > " + Result;
                }
                if (Bo.Result == MainForm.Result.Won)
                {
                    Result += " [ Win ]";
                }
                else if (Bo.Result == MainForm.Result.Lost)
                {
                    Result += " [ Lost ]";
                }
                else if (Bo.Result == MainForm.Result.Tied)
                {
                    Result += " [ Tied ]";
                }
                return Result;
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
                if (value == Result.None)
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
        public MainForm()
        {
            InitializeComponent();
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
                    ChooseChess(Case[i].GetChessPos(Chess.Preferred));
                    return true;
                }
            }
            return false;
        }
        private void CheckResponse()
        {
            Board[] PMask = Mask.Boards;
            foreach (Pack Case in Cases)
            {
                if (ProcessResponse(Case.Boards, PMask)) { return; }
            }
            Board[] PWonCN = WonCN.Boards;
            Board[] PLostCN = LostCN.Boards;
            Board[] PMaskCN = MaskCN.Boards;
            Board[] PWonCM = WonCM.Boards;
            Board[] PLostCM = LostCM.Boards;
            Board[] PMaskCM = MaskCM.Boards;
            Board[] PWonSN = WonSN.Boards;
            Board[] PLostSN = LostSN.Boards;
            Board[] PMaskSN = MaskSN.Boards;
            Board[] PWonSM = WonSM.Boards;
            Board[] PLostSM = LostSM.Boards;
            Board[] PMaskSM = MaskSM.Boards;
            if (ProcessResponse(PLostCN, PMaskCN)) { return; }
            if (ProcessResponse(PLostCM, PMaskCM)) { return; }
            if (ProcessResponse(PLostSN, PMaskSN)) { return; }
            if (ProcessResponse(PLostSM, PMaskSM)) { return; }
            if (ProcessResponse(PWonCN, PMaskCN)) { return; }
            if (ProcessResponse(PWonCM, PMaskCM)) { return; }
            if (ProcessResponse(PWonSN, PMaskSN)) { return; }
            if (ProcessResponse(PWonSM, PMaskSM)) { return; }
            ChooseChess(Bo.GetChessPos(Chess.None));
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
            if (Re != Result.None) { return; }
            Board[] PWonC = WonC.Boards;
            Board[] PLostC = LostC.Boards;
            Board[] PMaskC = MaskC.Boards;
            Board[] PWonS = WonS.Boards;
            Board[] PLostS = LostS.Boards;
            Board[] PMaskS = MaskS.Boards;
            if (ProcessResult(PWonC, PMaskC, Result.Won)) { return; }
            if (ProcessResult(PWonS, PMaskS, Result.Won)) { return; }
            if (ProcessResult(PLostC, PMaskC, Result.Lost)) { return; }
            if (ProcessResult(PLostS, PMaskS, Result.Lost)) { return; }
            if (Bo.Round == 9u) { Re = Result.Tied; }
        }
        private void NewGame(Mode Mode)
        {
            if (Mode == Mode.StartupMode || Mo == Mode) { Tu = Turn.Unspecified; }
            else if (Mode == Mode.ConjugateMode) { Mo = Bo.ConjugateMode; }
            else if (Mode == Mode.ConfigurateMode) { Mo = Bo.ConfigurateMode; }
            else { Mo = Mode; }
                ButtonReset.Enabled = false;
            for (int i = 1; i <= 9; ++i)
            {
                Co[i].Text = string.Empty;
                Co[i].ForeColor = Color.Black;
            }
            if (Bo.OnDefenderSide && !Bo.InDebugMode) { CheckResponse(); }
        }
        private void PutChess(Control Target)
        {
            ButtonReset.Enabled = true;
            int i = Array.IndexOf(Co, Target);
            if (Bo[i] == Chess.None && Re == Result.None)
            {
                if (Tu == Turn.User)
                {
                    Bo[i] = Chess.O;
                    Target.Text = " O ";
                    Target.ForeColor = Color.Green;
                    Tu = Turn.Response;
                    CheckResult();
                    if (!Bo.InDebugMode && Re == Result.None)
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
        private void ButtonSwitch_Click(object sender, EventArgs e)
        {
            NewGame(Mode.ConjugateMode);
        }
        private void ButtonReset_Click(object sender, EventArgs e)
        {
            NewGame(Mode.StartupMode);
        }
        private void MainForm_Load(object sender, EventArgs e)
        {
            NewGame(Mode.StartupMode);
        }
        private void Button_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.KeyCode == Keys.D && !Bo.InDebugMode)
            {
                NewGame(Bo.ConfigurateMode);
            }
            else if (e.KeyCode == Keys.Escape && Bo.InDebugMode)
            {
                NewGame(Bo.ConfigurateMode);
            }
        }
        private void Button_Click(object sender, EventArgs e)
        {
            PutChess(sender as Control);
        }
        #endregion
    }
}

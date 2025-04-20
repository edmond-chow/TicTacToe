pyside6-rcc "./TicTacToe.qrc" -o "./TicTacToe.py"
pyinstaller --onefile --windowed --add-data="TicTacToe.ico;." --name="TicTacToe.exe" --icon="./TicTacToe.ico" --version-file="version.txt" "./Program.py"

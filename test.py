import sys

def bachets_game(n, moves):
    win = [False] * (n + 1)
    for i in range(1, n + 1):
        for move in moves:
            if move <= i and not win[i - move]:
                win[i] = True
                break
    return win[n]

def main():
    for line in sys.stdin:
        if not line.strip():
            continue
        parts = list(map(int, line.split()))
        n = parts[0]
        m = parts[1]
        moves = parts[2:]
        assert len(moves) == m
        if bachets_game(n, moves):
            print("Stan wins")
        else:
            print("Ollie wins")

if __name__ == "__main__":
    main()

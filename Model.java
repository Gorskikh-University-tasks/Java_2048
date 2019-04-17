package com.javarush.task.task35.task3513;

import java.util.*;

/**
 * Будет ответственен за все манипуляции производимые с игровым полем.
 */
public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles= new Tile[FIELD_WIDTH][FIELD_WIDTH];
    protected int score;
    protected int maxTile;
    protected boolean isSaveNeeded = true;
    private Stack<Tile[][]> previousStates = new Stack<>();
    private Stack<Integer> previousScores = new Stack<>();

    public Model() {
        score = 0;
        maxTile = 0;
        resetGameTiles();
    }

    public void autoMove()
    {
        PriorityQueue<MoveEfficiency> queue = new PriorityQueue<>(4, Collections.reverseOrder());
        queue.offer(getMoveEfficiency(this::left));
        queue.offer(getMoveEfficiency(this::right));
        queue.offer(getMoveEfficiency(this::up));
        queue.offer(getMoveEfficiency(this::down));
        if (queue.size() != 0)
            queue.poll().getMove().move();
    }

    public MoveEfficiency getMoveEfficiency(Move move)
    {
        move.move();
        MoveEfficiency moveEfficiency;
        if(hasBoardChanged())
            moveEfficiency =  new MoveEfficiency(getEmptyTiles().size(), score, move);
        else
            moveEfficiency = new MoveEfficiency(-1, 0, move);
        rollback();
        return moveEfficiency;
    }

    public boolean hasBoardChanged() {
        int currentTiles = 0;
        int previousTiles = 0;
        if (previousStates.empty()) return false;
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles[0].length; j++) {
                previousTiles += previousStates.peek()[i][j].value;
                currentTiles += gameTiles[i][j].value;
            }
        }
        return currentTiles != previousTiles;
    }

    public void randomMove()
    {
        int rand = ((int)(Math.random() * 100)) % 4;
        switch (rand)
        {
            case 0: left(); break;
            case 1: right(); break;
            case 2: up(); break;
            case 3: down(); break;
        }
    }

    private void saveState(Tile[][] gameTiles)
    {
        Tile[][] tmp = new Tile[gameTiles.length][gameTiles[0].length];
        for(int i = 0; i < gameTiles.length; i++)
            for(int j = 0; j < gameTiles[0].length; j++)
                tmp[i][j] = new Tile(gameTiles[i][j].value);
        previousStates.push(tmp);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public void rollback()
    {
        if(!previousScores.empty() && !previousStates.empty()){
            gameTiles = previousStates.pop();
            score = previousScores.pop();
        }
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    public boolean canMove(){
        if(getEmptyTiles().size() > 0) return true;
        for(int i = 0; i < gameTiles.length - 1; i++)
        {
            for(int j = 0; j < gameTiles[0].length; j++)
            {
                if(gameTiles[i][j].value == gameTiles[i+1][j].value)
                    return true;
                else if(j+1 < gameTiles[0].length
                        && gameTiles[i][j].value == gameTiles[i][j+1].value) return true;
            }
        }

        return false;
    }

    protected void resetGameTiles()
    {
        for(int i = 0; i < FIELD_WIDTH; i++){
            for(int j = 0; j < FIELD_WIDTH; j++)
                gameTiles[i][j] = new Tile();
        }

        addTile();
        addTile();
    }

    private void addTile()
    {
        List<Tile> emptyTiles = getEmptyTiles();
        if(emptyTiles.size() == 0) return;
        Tile randomTile = emptyTiles.get((int)(Math.random() * emptyTiles.size()));
        randomTile.value = Math.random() < 0.9 ? 2 : 4;
    }

    private List<Tile> getEmptyTiles() {
        ArrayList<Tile> list = new ArrayList<>();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++)
                if (gameTiles[i][j].isEmpty())
                    list.add(gameTiles[i][j]);
        }

        return list;
    }

    private boolean compressTiles(Tile[] tiles) {
        boolean flag = false;
        int zeroIndex = -1;
        for(int i = 0; i < tiles.length; i++)
        {
            if(tiles[i].isEmpty() && zeroIndex == -1) zeroIndex = i;
            else if(!tiles[i].isEmpty() && zeroIndex != -1)
            {
                tiles[zeroIndex].value = tiles[i].value;
                tiles[i].value = 0;
                flag = true;
                zeroIndex++;
            }
        }
        return flag;
    }

    private boolean mergeTiles(Tile[] tiles) {
        boolean flag = false;
        for(int i = 0; i < tiles.length - 1; i++)
        {
            if(tiles[i].isEmpty()) continue;
            if(tiles[i].value == tiles[i+1].value)
            {
                tiles[i].value *= 2;
                tiles[i+1].value = 0;
                score += tiles[i].value;
                maxTile = maxTile > tiles[i].value ? maxTile : tiles[i].value;
                flag = true;
            }
        }

        if(flag) compressTiles(tiles);
        return flag;
    }

    public void left()
    {
        if(isSaveNeeded)
            saveState(gameTiles);

        isSaveNeeded = true;
        boolean isChanged = false;

        for(int i = 0; i < gameTiles.length; i++)
        {
            if(compressTiles(gameTiles[i]) | mergeTiles(gameTiles[i]))
                isChanged = true;
        }
        if(isChanged) addTile();
    }

    public void right()
    {
        saveState(gameTiles);
        rotateMatrix();
        rotateMatrix();
        left();
        rotateMatrix();
        rotateMatrix();
    }

    public void up()
    {
        saveState(gameTiles);
        rotateMatrix();
        rotateMatrix();
        rotateMatrix();
        left();
        rotateMatrix();
    }

    public void down()
    {
        saveState(gameTiles);
        rotateMatrix();
        left();
        rotateMatrix();
        rotateMatrix();
        rotateMatrix();
    }

    private void rotateMatrix(){
        Tile[][] tmp = new Tile[gameTiles.length][gameTiles[0].length];
        for(int i = 0; i < gameTiles.length; i++)
            for (int j = 0; j < gameTiles[0].length; j++)
                tmp[j][3 - i] = gameTiles[i][j];

        gameTiles = tmp;
    }
}

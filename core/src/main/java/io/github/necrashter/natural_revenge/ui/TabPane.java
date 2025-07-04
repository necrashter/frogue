package io.github.necrashter.natural_revenge.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

import io.github.necrashter.natural_revenge.Main;

public class TabPane {
    public Table tabButtons = new Table();
    public final Table rootTable;

    public Array<Table> tabContents = new Array<>();

    public TabPane(Table rootTable) {
        this.rootTable = rootTable;
    }

    public void addPane(String name, Table content) {
        TextButton button = new TextButton(name, Main.skin);
        int index = tabContents.size;
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                changePane(index);
            }
        });
        tabButtons.add(button).padRight(8);
        tabContents.add(content);
    }

    public void changePane(int index) {
        rootTable.clear();
        rootTable.add(tabButtons).growX().row();
        rootTable.add(tabContents.get(index)).grow();
    }
}

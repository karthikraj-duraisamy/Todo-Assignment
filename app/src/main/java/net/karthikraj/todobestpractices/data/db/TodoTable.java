package net.karthikraj.todobestpractices.data.db;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by root on 10/11/16.
 */

@Table(name = "TodoTable")
public class TodoTable extends Model{

    @Column(name = "name")
    public String name;
    @Column(name = "state")
    public int state;
}

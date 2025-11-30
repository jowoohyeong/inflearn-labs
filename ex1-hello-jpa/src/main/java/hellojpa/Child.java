package hellojpa;

import jakarta.persistence.*;

@Entity
public class Child {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String username;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Parent parent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }
}

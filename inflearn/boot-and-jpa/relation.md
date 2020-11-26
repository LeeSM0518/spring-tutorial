# 1 : 1

* DB 접근이 많은 쪽이 연관 관계의 주인이 된다.

  * **Order (주인)**

    ```java
    @OneToOne(fetch = LAZY, cascade = ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;
    ```

  * **Delivery (거울)**

    ```java
    @OneToOne(mappedBy = "delivery", fetch = LAZY)
    private Order order;
    ```

> 주인 : **@JoinColumn(name = "column(상대 테이블의 조인할 컬럼)")**
>
> * 상대 테이블과 조인될 상대 테이블의 기본 키인 컬럼 작성
>
> 거울 : **@OneToOne(mappedBy = "object(상대 엔티티의 매핑될 객체)")**
>
> * 상대 테이블의 외래 키로 엮일 객체 작성
>
> `fetch = LAZY` : @~ToOne(OneToOne, ManyToOne) 관계는 기본이 즉시로딩이므로 직접 지연로딩으로 설정해야 한다.

<br>

# 1 : N (양방향 연관관계)

* FK 를 갖고 있는 엔티티가 주인이 된다. 즉, 1:N 중에 N 쪽이 주인이 된다.

  * **Order (주인)**

    ```java
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    ```

  * **Member (거울)**

    ```java
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();
    ```

<br>

# N : 1 (단방향 연관관계)

* FK 를 갖고 있는 엔티티가 주인이 된다. 즉, 1:N 중에 N 쪽이 주인이 된다.

  * **OrderItem (주인)**

    ```java
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "item_id")
    private Item item;
    ```

<br>

# N : M 

* 다대다 관계는 중간 관계를 테이블로 매핑해줘야 한다.

  * **Category**

    ```java
    @ManyToMany
    @JoinTable(name = "category_item",
              joinColumn = @JoinColumn(name = "category_id"),
              inverseJoinColumn = @JoinColumn(name = "item_id"))
    private List<Item> items = new ArrayList<>();
    ```

    
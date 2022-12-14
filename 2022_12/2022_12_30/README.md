# DB Index

### Index란?

-   데이터베이스 테이블에 대한 검색 속도를 높여주는 자료구조
-   특정 컬럼에 인덱스를 생성하면 해당 컬럼의 데이터들을 정려하여 별도의 메모리 공간에 물리적 주소와 함께 저장
-   쿼리문을 통해 데이터를 조회할 때 옵티마이저에서 판단하여 생성된 인덱스를 사용
-   인덱스를 활용하게 되면 인덱스에 저장되어있는 데이터의 물리적 주소로 가서 데이터를 조회하기 때문에 검색속도가 향상된다

![image](https://user-images.githubusercontent.com/96561194/210071624-56626894-edf9-4e07-a962-389d910401e1.png)

-   책의 목차와 유사한 역할을 함

### 인덱스를 사용하는 이유

-   인덱의 가장 큰 특징은 데이터가 정렬되어있다는 점
-   이 특징으로 인해 조건 검색 영역에서 굉장한 장점을 가진다

### Where 절에서의 효율

-   테이블을 만들고 안에 데이터가 쌓이게 되면 테이블의 레코드는 내부적으로 순서가 없이 저장됨
-   Where 절에 특정 조건에 맞는 데이터를 찾아낼때도 레코드의 처음부터 끝까지 다읽어서 검색 조건과 맞는지 비교해야함 (풀 테이블 스캔)
-   인덱스 테이블은 데이터가 정렬되어있기 때문에 해당 조건에 맞는 데이터를 빠르게 찾는다

### Order by 절에서의 효율

-   Order by는 데이터를 정렬하는 과정을 거치기 때문에 굉장히 부하가 많이걸림
-   1차적으로 메모리에서 정렬, 메모리를 넘어간다면 디스크I/O도 추가적으로 발생
-   Index 는 이미 정렬되어있기 때문에 가져오기만 하면 된다 (전반적이 자원 소모 X)

### 인덱스의 단점

-   **가장 큰 문제점은 정렬 상태를 계속 유지시켜 줘야 한다는점**
-   INSERT, DELETE, UPDATE 을 통해 데이터가 추가되거나 값이 바뀌면 INDEX 테이블 내의 값을 가시 정렬해야함
-   Index 테이블, 원본 테이블 두군데에 수정작업을 해줘야함
-   검색시에도 Index가 무조건적인 장점을 가지는 것이 아니다
    -   테이블 전체 데이터의 10~15% 이하의 데이터를 처리하는 경우에만 효율적
    -   인덱스를 관리하기 위해서는 데이터베이스의 약 10%에 해당하는 저장공간이 추가적으로 필요
    -   인덱스 생성은 신중히 해야함

### 인덱스의 관리

-   인덱스는 INSERT, UPDATE, DELETE 수행시 데이터를 계속 정렬하기 때문에 부하가 발생
-   부하를 최소화 하기 위해 다음과 같은 방법을 사용
    -   INSERT : 새로운 데이터에 대한 인덱스 추가
    -   DELETE : 삭제하는 데이터의 인덱스를 사용하지 않는 작업 진행 (인덱스 삭제 X)
    -   UPDATE : 기존에 인덱스를 사용안함 처리후 , 갱신된 데이터에 대해 인덱스를 추가

### 인덱스 생성 전략

-   인덱스는 장점이 큼과 동시에 단점도 크기 때문에 신중히 사용해야함
-   다음과 같은 조건들이 만족할 때 인덱스를 생성하는 것이 좋다

1. 조건절에 자주 등장하는 컬럼
2. 항상 = 으로 비교되는 컬럼
3. 중복되는 데이터가 최소한인 컬럼 (분포도가 좋은 컬럼)
4. ORDER BY 절이 자주 사용되는 컬럼
5. 조인 조건으로 자주 사용되는 컬럼

### 인덱스 리빌드

**DBMS에서 주로 사용하는 인덱스 구조**

**B \* Tree 인덱스**

![image](https://user-images.githubusercontent.com/96561194/210071714-9702581c-dbc2-4319-a72e-c4b549579389.png)

**인덱스를 리빌드 하는 이유**

-   삽입, 수정, 삭제등이 오랫동안 일어나다보면 트리의 한쪽이 무거워져 전체적인 트리의 깊이가 깊어짐
-   이러한 현상이 일어나면 인덱스의 검색속도가 떨어지므로 주기적으로 리빌딩 작업을 거쳐야함

### 인덱스를 신중하게 사용해야 하는 이유

-   많은 양의 데이터를 처리하다보면 성능 이슈가 많이 발생한다
-   주로 인덱스 추가 생성을 해결책으로 선택함
-   인덱스를 참조하는 하나의 쿼리문은 빨라지겠지만 전체적인 데이터베이스에 성능부하가 생김
-   인덱스 추가는 마지막 수단으로 사용하고 보다 효율적인 쿼리문을 짜는 방향으로 잡는것이 좋음

</br>
<b>인프런 김영한님 강의 참고</b>

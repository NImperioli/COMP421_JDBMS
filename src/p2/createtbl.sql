-- Connect to the database
CONNECT TO COMP421;

-- Create tables without foreign key dependencies first

-- Developer Table
CREATE TABLE Developer (
    did INT NOT NULL,
    dname VARCHAR(60) NOT NULL,
    demail VARCHAR(75) NOT NULL UNIQUE,
    dphone VARCHAR(15),
    PRIMARY KEY (did)
);

-- Publisher Table
CREATE TABLE Publisher (
    pid INT NOT NULL,
    pname VARCHAR(60) NOT NULL,
    pemail VARCHAR(75) NOT NULL UNIQUE,
    pphone VARCHAR(15),
    PRIMARY KEY (pid)
);

-- Game Table
CREATE TABLE Game (
    gid INT NOT NULL,
    date DATE NOT NULL,
    rating DECIMAL(3, 2) CHECK (rating >= 0.00 AND rating <= 5.00),
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0.00),
    gname VARCHAR(60) NOT NULL,
    PRIMARY KEY (gid)
);

-- Category Table
CREATE TABLE Category (
    cname VARCHAR(30) NOT NULL,
    PRIMARY KEY (cname)
);

-- Users Table
CREATE TABLE Users (
    uid INT NOT NULL,
    uemail VARCHAR(30) NOT NULL UNIQUE,
    password VARCHAR(30) NOT NULL,
    PRIMARY KEY (uid)
);

-- BillingInfo Table
CREATE TABLE BillingInfo (
    uid INT NOT NULL,
    address VARCHAR(30),
    uname VARCHAR(20) NOT NULL,
    payMethod VARCHAR(15) NOT NULL CHECK (payMethod IN ('Credit Card', 'PayPal', 'Debit Card', 'Gift Card')),
    PRIMARY KEY (uid),
    FOREIGN KEY (uid) REFERENCES Users(uid)
);

-- Transactions Table
CREATE TABLE Transactions (
    tid INT NOT NULL,
    date DATE NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(10) NOT NULL CHECK (status IN ('Failed', 'Pending', 'Completed')),
    uid INT NOT NULL,
    PRIMARY KEY (tid),
    FOREIGN KEY (uid) REFERENCES Users(uid)
);

-- Discount Table
CREATE TABLE Discount (
    gid INT NOT NULL,
    startDate DATE NOT NULL,
    endDate DATE NOT NULL,
    percentOff DECIMAL(5, 2) NOT NULL CHECK (percentOff >= 0.00 AND percentOff <= 100.00),
    PRIMARY KEY (gid, startDate),
    FOREIGN KEY (gid) REFERENCES Game(gid),
    CHECK (endDate >= startDate)
);

-- MakePublish Table
CREATE TABLE MakePublish (
    gid INT NOT NULL,
    did INT NOT NULL,
    pid INT NOT NULL,
    PRIMARY KEY (gid),
    FOREIGN KEY (gid) REFERENCES Game(gid),
    FOREIGN KEY (did) REFERENCES Developer(did),
    FOREIGN KEY (pid) REFERENCES Publisher(pid)
);

-- Wish Table
CREATE TABLE Wish (
    uid INT NOT NULL,
    gid INT NOT NULL,
    PRIMARY KEY (uid, gid),
    FOREIGN KEY (uid) REFERENCES Users(uid),
    FOREIGN KEY (gid) REFERENCES Game(gid)
);

-- Own Table
CREATE TABLE Own (
    uid INT NOT NULL,
    gid INT NOT NULL,
    PRIMARY KEY (uid, gid),
    FOREIGN KEY (uid) REFERENCES Users(uid),
    FOREIGN KEY (gid) REFERENCES Game(gid)
);

-- InCart Table
CREATE TABLE InCart (
    uid INT NOT NULL,
    gid INT NOT NULL,
    PRIMARY KEY (uid, gid),
    FOREIGN KEY (uid) REFERENCES Users(uid),
    FOREIGN KEY (gid) REFERENCES Game(gid)
);

-- Friend Table
CREATE TABLE Friend (
    uid1 INT NOT NULL,
    uid2 INT NOT NULL,
    status VARCHAR(10) NOT NULL CHECK (status IN ('Pending', 'Accepted', 'Blocked', 'Rejected')),
    PRIMARY KEY (uid1, uid2),
    FOREIGN KEY (uid1) REFERENCES Users(uid),
    FOREIGN KEY (uid2) REFERENCES Users(uid),
    CHECK (uid1 != uid2)
);

-- BelongsTo Table
CREATE TABLE BelongsTo (
    gid INT NOT NULL,
    cname VARCHAR(25) NOT NULL,
    PRIMARY KEY (gid, cname),
    FOREIGN KEY (gid) REFERENCES Game(gid),
    FOREIGN KEY (cname) REFERENCES Category(cname)
);

-- Buy Table
CREATE TABLE Buy (
    uid INT NOT NULL,
    gid INT NOT NULL,
    tid INT NOT NULL,
    PRIMARY KEY (uid, gid, tid),
    FOREIGN KEY (uid) REFERENCES Users(uid),
    FOREIGN KEY (gid) REFERENCES Game(gid),
    FOREIGN KEY (tid) REFERENCES Transactions(tid)
);
function J = projectToSimplex(csv_name, output_csv_name, le_pair_csv_name, total)

    [left, right] = textread(le_pair_csv_name, '%s %s', "delimiter", "\t");
    [mtd, freq] = textread(csv_name, '%s %f', "delimiter", "\t");

    ieqMtx = zeros(length(left), length(mtd));
    for i = 1:length(left)
        idx1 = find(string(mtd) == left{i});
        idx2 = find(string(mtd) == right{i});
        ieqMtx(i, idx1) = 1;
        ieqMtx(i, idx2) = -1;
    end

    Y = freq;
    n = length(Y);
    m = length(left);
    H = sparse(eye(n));
    f = zeros(n, 1);
    A = ieqMtx;
    b = zeros(m, 1);
    Aeq = ones(1, n);
    beq = total - ones(1, n) * Y;
    lb = -Y;
    [x, fval, exitflag, output, lambda] = quadprog(H, f, A, b, Aeq, beq, lb, []);
    z = x + Y;
    exitflag
    output

    fid = fopen(output_csv_name,'w');
    for i = 1:n
        fprintf(fid, ' %s\t %f \n', mtd{i}, z(i));
    end
    fclose(fid);

end
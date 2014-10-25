    def tuple[Z, Y, X, W, V](x: X, y: Y): Z => W => V => V ** W ** X ** Y ** Z =
      z => w => v => ((((v, w), x), y), z)

    def program[F[_]: Res](implicit id_sub_f: Identity <= F,
      supplier_sub_f: Supplier <= F,
      withFile_sub_f: WithFile <= F): Free[F, Int ** Int ** Int ** String ** String] = {
      supply({ randomVerboseSleep("supplier")(3000); 1 }) and {
        supply({ randomVerboseSleep("supplier")(3000); 2 }) and {
          supply({ randomVerboseSleep("supplier")(3000); 3 }) bnd { x =>
            readFile(fooFile) bnd { barFilePath =>
              readFile(new File(barFilePath)) end {
                tuple(x, barFilePath)
              }
            }
          }
        }
      }
    }

    val future: Future[Int ** Int ** Int ** String ** String] =
      program[({ type λ[ω] = Identity[ω] ++ Supplier[ω] ++ WithFile[ω] })#λ].fold

    println(future(es).get)

    es.shutdown()

    def tuple[Z, Y, X, W, V]: Z => Y => X => W => V => V ** W ** X ** Y ** Z =
      z => y => x => w => v => ((((v, w), x), y), z)

    /*
Referential transparency illustrated
*/
    def program[F[_]: Res](implicit id_sub_f: Identity <= F,
      supplier_sub_f: Supplier <= F,
      withFile_sub_f: WithFile <= F): Free[F, Int ** Int ** Int ** String ** String] = {
      supply({ randomVerboseSleep("supplier")(3000); 1 }) and {
        supply({ randomVerboseSleep("supplier")(3000); 2 }) and {
          supply({ randomVerboseSleep("supplier")(3000); 3 }) and {
            readFile(fooFilePath) and {
              readFile(barFilePath) end {
                tuple
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

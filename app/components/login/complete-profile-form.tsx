import type { ReactNode } from "react";
import { useMsal } from "@azure/msal-react";
import { Controller, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { ArrowIcon } from "../landing/arrow-icon";
import { AlertIcon } from "../ui/icons";
import { completeProfile } from "../../lib/api";
import { getFieldErrors } from "../../lib/api-errors";
import { acquireApiToken } from "../../lib/auth-token";
import type { UserProfile } from "../../types/user";
import { profileSchema, type ProfileData } from "../../lib/profile-schema";

function inputClass(hasError: boolean) {
  return `h-13 w-full rounded-lg border bg-white px-4 text-base text-ink transition-colors duration-150 placeholder:text-muted focus:border-ink ${
    hasError ? "border-accent-deep" : "border-line-strong hover:border-ink/70"
  }`;
}

function formatCardNumber(digits: string) {
  return digits.replace(/(\d{4})(?=\d)/g, "$1 ");
}

function formatExpiry(value: string) {
  const digits = value.replace(/\D/g, "").slice(0, 4);
  return digits.length > 2 ? `${digits.slice(0, 2)}/${digits.slice(2)}` : digits;
}

type FieldProps = {
  id: string;
  label: string;
  error?: string;
  hint?: string;
  children: ReactNode;
};

function Field({ id, label, error, hint, children }: FieldProps) {
  return (
    <div className="flex flex-col gap-2">
      <label htmlFor={id} className="text-sm font-semibold">{label}</label>
      {children}
      {error ? (
        <p className="enter flex items-start gap-1.5 text-sm leading-5 text-accent-deep">
          <AlertIcon className="mt-px size-4 shrink-0" />
          {error}
        </p>
      ) : (
        hint && <p className="text-sm leading-5 text-muted">{hint}</p>
      )}
    </div>
  );
}

type Props = {
  profile: UserProfile;
  onCompleted: (profile: UserProfile) => void;
};

export function CompleteProfileForm({ profile, onCompleted }: Props) {
  const { instance, accounts } = useMsal();
  const {
    register,
    control,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<ProfileData>({
    resolver: zodResolver(profileSchema),
    defaultValues: { rut: "", birthDate: "", cardNumber: "", cardHolderName: "", cardExpiry: "" },
  });
  const today = new Date().toISOString().slice(0, 10);

  // Only runs once the Zod schema passed.
  async function onSubmit(data: ProfileData) {
    try {
      const token = await acquireApiToken(instance, accounts[0]);
      if (!token) return;
      onCompleted(await completeProfile(token, data));
    } catch (err) {
      console.error(err);
      const fieldErrors = getFieldErrors(err);
      if (fieldErrors) {
        for (const [field, message] of Object.entries(fieldErrors)) {
          setError(field as keyof ProfileData, { message });
        }
      } else {
        setError("root", { message: "No pudimos guardar tus datos. Intenta de nuevo." });
      }
    }
  }

  return (
    <section className="rise mx-auto w-full max-w-md py-5 sm:py-10 lg:py-12">
      <p className="mb-5 text-xs font-bold tracking-[0.16em] text-muted uppercase">Un último paso</p>
      <h1 className="font-display text-5xl leading-[0.92] font-bold tracking-[-0.02em] uppercase sm:text-6xl">
        Completa<br />tu <span className="text-accent-deep">perfil.</span>
      </h1>
      <p className="mt-5 text-base leading-7 text-muted">
        {profile.name ? `Hola, ${profile.name}. ` : ""}Necesitamos estos datos para que puedas reservar.
      </p>

      <form onSubmit={handleSubmit(onSubmit)} noValidate className="mt-8 space-y-6">
        <Field id="profile-rut" label="RUT" error={errors.rut?.message} hint="Sin puntos ni guion.">
          <Controller
            name="rut"
            control={control}
            render={({ field }) => (
              <input
                id="profile-rut"
                ref={field.ref}
                onBlur={field.onBlur}
                value={field.value}
                onChange={(e) => field.onChange(e.target.value.replace(/[^0-9kK]/g, "").toUpperCase())}
                autoComplete="off"
                maxLength={9}
                placeholder="123456785"
                className={inputClass(!!errors.rut)}
              />
            )}
          />
        </Field>

        <Field id="profile-birthDate" label="Fecha de nacimiento" error={errors.birthDate?.message}>
          <input
            id="profile-birthDate"
            {...register("birthDate")}
            type="date"
            max={today}
            className={inputClass(!!errors.birthDate)}
          />
        </Field>

        <Field id="profile-cardNumber" label="Número de tarjeta" error={errors.cardNumber?.message}>
          <Controller
            name="cardNumber"
            control={control}
            render={({ field }) => (
              <input
                id="profile-cardNumber"
                ref={field.ref}
                onBlur={field.onBlur}
                value={formatCardNumber(field.value)}
                onChange={(e) => field.onChange(e.target.value.replace(/\D/g, "").slice(0, 19))}
                inputMode="numeric"
                autoComplete="cc-number"
                placeholder="4111 1111 1111 1111"
                className={inputClass(!!errors.cardNumber)}
              />
            )}
          />
        </Field>

        <div className="grid gap-6 sm:grid-cols-[1fr_8rem]">
          <Field id="profile-cardHolderName" label="Nombre del titular" error={errors.cardHolderName?.message}>
            <input
              id="profile-cardHolderName"
              {...register("cardHolderName")}
              autoComplete="cc-name"
              placeholder="Como aparece en la tarjeta"
              className={inputClass(!!errors.cardHolderName)}
            />
          </Field>

          <Field id="profile-cardExpiry" label="Vencimiento" error={errors.cardExpiry?.message}>
            <Controller
              name="cardExpiry"
              control={control}
              render={({ field }) => (
                <input
                  id="profile-cardExpiry"
                  ref={field.ref}
                  onBlur={field.onBlur}
                  value={field.value}
                  onChange={(e) => field.onChange(formatExpiry(e.target.value))}
                  inputMode="numeric"
                  autoComplete="cc-exp"
                  placeholder="MM/AA"
                  className={inputClass(!!errors.cardExpiry)}
                />
              )}
            />
          </Field>
        </div>

        <button
          type="submit"
          disabled={isSubmitting}
          className="press group mt-2 flex min-h-13 w-full cursor-pointer items-center justify-between gap-4 rounded-lg bg-accent px-5 py-4 text-sm font-bold text-ink hover:bg-accent-hover disabled:cursor-not-allowed disabled:opacity-50"
        >
          {isSubmitting ? "Guardando…" : "Guardar y continuar"} <ArrowIcon className="nudge size-5" />
        </button>

        {errors.root && (
          <p role="alert" className="enter flex items-start gap-2.5 rounded-xl bg-white px-4 py-3 text-sm leading-6 text-accent-deep shadow-card">
            <AlertIcon className="mt-0.5 size-5 shrink-0" />
            {errors.root.message}
          </p>
        )}
      </form>
    </section>
  );
}

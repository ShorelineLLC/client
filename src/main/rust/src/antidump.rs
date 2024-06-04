use std::arch::x86_64::{CpuidResult, __cpuid, _rdtsc};
use std::ptr::null;
use obfstr::obfstr;

pub unsafe fn run_antidump_checks() -> String
{
    if platform::is_debugger_present()
    {
        return String::from(obfstr!("Native debugger present"));
    }

    return String::from(obfstr!("Safe"));
}

#[cfg(target_os = "windows")]
mod platform
{
    extern crate winapi;
    use winapi::um::debugapi::IsDebuggerPresent;

    // amazing code
    // we will be adding more checks later
    pub fn is_debugger_present() -> bool
    {
        if IsDebuggerPresent() != 0
        {
            return true;
        }

        return false;
    }
}

#[cfg(target_os = "macos")]
mod platform
{
    pub fn is_debugger_present() -> bool
    {
        panic!("not implemented!")
    }
}

#[cfg(target_os = "linux")]
mod platform
{
    pub fn is_debugger_present() -> bool
    {
        panic!("not implemented")
    }
}

// pub unsafe fn inside_vm() -> bool
// {
//     inside_vm_custom(5, 100, 5, 1_000)
// }
//
// pub unsafe fn inside_vm_custom(low: usize,
//                                samples: usize,
//                                high: usize,
//                                threshold: u64) -> bool
// {
//     cpuid_cycle_count_avg(low, samples, high) > threshold
// }
//
// pub unsafe fn cpuid_cycle_count_avg(low: usize,
//                              samples: usize,
//                              high: usize) -> u64
// {
//     let mut tsc1: u64;
//     let mut tsc2: u64;
//     let mut cycles: Vec<u64> = vec![];
//     let mut cpuid = CpuidResult
//     {
//         eax: 0,
//         ebx: 0,
//         ecx: 0,
//         edx: 0,
//     };
//
//     for _ in 0..(low + samples + high)
//     {
//         tsc1 = _rdtsc();
//         cpuid = __cpuid(0);
//         tsc2 = _rdtsc();
//
//         cycles.push(tsc2 - tsc1);
//     }
//
//     std::ptr::read_volatile(&cpuid);
//
//     cycles.sort_unstable();
//     let cycles_without_outliers = &cycles[low..low + samples];
//
//     let avg = cycles_without_outliers.iter().sum::<u64>() / std::cmp::max(samples as u64, 1);
//     return avg;
// }